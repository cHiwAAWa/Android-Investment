package tw.edu.niu.investment_android.ui.outputs

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import tw.edu.niu.investment_android.databinding.FragmentOutputsBinding
import java.io.File
import java.io.FileWriter
import java.util.Locale
import android.app.AlertDialog

class OutputsFragment : Fragment() {
    private var _binding: FragmentOutputsBinding? = null
    private val binding get() = _binding!!
    private lateinit var assetAdapter: AssetAdapter
    private val assets = mutableListOf<Asset>()
    private val client = OkHttpClient()
    // 伺服器 Tailscale IP
    private val serverBaseUrl = "ws://100.79.72.71:3030"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOutputsBinding.inflate(inflater, container, false)
        loadAssetsFromToml()

        binding.recyclerViewOutputs.layoutManager = LinearLayoutManager(requireContext())
        assetAdapter = AssetAdapter(
            assets,
            onEditClick = { asset, pos -> showEditDialog(asset, pos) },
            onDeleteClick = { asset, pos -> confirmAndDelete(asset, pos) }
        )
        binding.recyclerViewOutputs.adapter = assetAdapter

        return binding.root
    }

    private fun loadAssetsFromToml() {
        assets.clear()
        val file = File(requireContext().filesDir, "portfolio.toml")
        if (!file.exists()) return

        var currentCategory = ""
        file.readLines().forEach { line ->
            when {
                line.startsWith("[") -> currentCategory =
                    line.removePrefix("[").removeSuffix("]").trim()
                line.contains("=") -> {
                    val (symbolRaw, amtRaw) = line.split("=").map { it.trim() }
                    val symbol = symbolRaw.removeSurrounding("\"").lowercase(Locale.getDefault())
                    assets.add(Asset(currentCategory, symbol, amtRaw))
                }
            }
        }
    }

    private fun showEditDialog(asset: Asset, position: Int) {
        val editText = EditText(requireContext()).apply {
            setText(asset.amount)
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
                // 僅允許數字與小數點，且只能一個小數點
                for (i in start until end) {
                    if (!source[i].isDigit() && source[i] != '.') return@InputFilter ""
                }
                val result = dest.toString().substring(0, dstart) +
                        source.subSequence(start, end) +
                        dest.toString().substring(dend)
                if (result.count { it == '.' } > 1) return@InputFilter ""
                null
            })
        }

        AlertDialog.Builder(requireContext())
            .setTitle("修改資產數量")
            .setView(editText)
            .setPositiveButton("確認") { _, _ ->
                val newAmtStr = editText.text.toString().trim()
                newAmtStr.toDoubleOrNull()?.let { newAmt ->
                    // 1. 更新資料結構與畫面
                    assets[position] = asset.copy(amount = newAmtStr)
                    assetAdapter.notifyItemChanged(position)
                    // 2. 重寫 portfolio.toml
                    persistToml()
                    // 3. 同步至伺服器
                    sendUpdateToServer(asset.category, asset.symbol, newAmt)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun confirmAndDelete(asset: Asset, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("刪除資產")
            .setMessage("確定要刪除 ${asset.symbol} 嗎？此操作無法復原。")
            .setPositiveButton("刪除") { _, _ ->
                // 1. 本地刪除
                assetAdapter.deleteAt(position)
                persistToml()
                // 2. 同步刪除至伺服器
                sendDeleteToServer(asset.category, asset.symbol)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /** 重寫 Toml 檔案 */
    private fun persistToml() {
        val file = File(requireContext().filesDir, "portfolio.toml")
        FileWriter(file).use { writer ->
            assets.groupBy { it.category }.forEach { (cat, list) ->
                writer.write("[$cat]\n")
                list.forEach { a ->
                    a.amount.toDoubleOrNull()?.let { writer.write("${a.symbol} = $it\n") }
                        ?: writer.write("${a.symbol} = \"${a.amount}\"\n")
                }
                writer.write("\n")
            }
        }
    }

    /** 修改數量（PUT 或 POST 視後端） */
    private fun sendUpdateToServer(category: String, symbol: String, amount: Double) {
        val url = "$serverBaseUrl/updateAsset"
        val payload = JSONObject().apply {
            put("category", category)
            put("symbol", symbol)
            put("amount", amount)
        }.toString()
        val body = payload.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        client.newCall(Request.Builder().url(url).post(body).build())
            .enqueue(simpleCallback("更新"))
    }

    /** 刪除資產 */
    private fun sendDeleteToServer(category: String, symbol: String) {
        val url = "$serverBaseUrl/deleteAsset"
        val payload = JSONObject().apply {
            put("category", category)
            put("symbol", symbol)
        }.toString()
        val body = payload.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        client.newCall(Request.Builder().url(url).post(body).build())
            .enqueue(simpleCallback("刪除"))
    }

    /** 共用的簡易 Callback */
    private fun simpleCallback(action: String) = object : okhttp3.Callback {
        override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
            // 失敗可記 log，或之後再做重試
            e.printStackTrace()
        }
        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            response.use {
                if (!it.isSuccessful) {
                    println("伺服器${action}失敗：${it.code}")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}