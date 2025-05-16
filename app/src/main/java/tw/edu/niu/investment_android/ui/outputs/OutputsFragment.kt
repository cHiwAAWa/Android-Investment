package tw.edu.niu.investment_android.ui.outputs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import tw.edu.niu.investment_android.databinding.FragmentOutputsBinding
import java.io.File
import android.app.AlertDialog
import android.widget.EditText
import java.io.FileWriter
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit

class OutputsFragment : Fragment() {

    private var _binding: FragmentOutputsBinding? = null
    private val binding get() = _binding!!
    private lateinit var assetAdapter: AssetAdapter
    private val assets = mutableListOf<Asset>()
    private var webSocket: WebSocket? = null
    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // 無限等待，適合串流
        .build()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOutputsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 載入資產資料
        loadAssetsFromToml()

        // 設置 RecyclerView
        binding.recyclerViewOutputs.layoutManager = LinearLayoutManager(requireContext())
        assetAdapter = AssetAdapter(assets) { asset, position ->
            showEditDialog(asset, position)
        }
        binding.recyclerViewOutputs.adapter = assetAdapter

        // 連接到 WebSocket
        connectWebSocket()

        return root
    }

    private fun connectWebSocket() {
        // 假設 Tailscale VM IP 為 100.79.72.71
        val wsUrl = "ws://100.79.72.71:3030/ws"
        val request = Request.Builder()
            .url(wsUrl)
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                requireActivity().runOnUiThread {
                    binding.tvStreamData.text = "WebSocket 已連線"
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                requireActivity().runOnUiThread {
                    binding.tvStreamData.text = "收到訊息：$text"
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                requireActivity().runOnUiThread {
                    binding.tvStreamData.text = "收到二進制訊息：${bytes.hex()}"
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                requireActivity().runOnUiThread {
                    binding.tvStreamData.text = "WebSocket 關閉：$reason"
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                requireActivity().runOnUiThread {
                    binding.tvStreamData.text = "WebSocket 錯誤：${t.message}"
                }
            }
        })
    }

    private fun loadAssetsFromToml() {
        assets.clear()
        val file = File(requireContext().filesDir, "portfolio.toml")

        if (file.exists()) {
            var currentCategory = ""
            file.readLines().forEach { line ->
                if (line.startsWith("[")) {
                    currentCategory = line.trim().removeSurrounding("[", "]")
                } else if (line.contains("=")) {
                    val (symbol, amount) = line.split("=").map { it.trim() }
                    val cleanSymbol = symbol.removeSurrounding("\"")
                    assets.add(Asset(currentCategory, cleanSymbol, amount))
                }
            }
        }
    }

    private fun showEditDialog(asset: Asset, position: Int) {
        val editText = EditText(requireContext()).apply {
            setText(asset.amount)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("修改資產數量")
            .setView(editText)
            .setPositiveButton("確認") { _, _ ->
                val newAmount = editText.text.toString().trim()
                if (newAmount.isNotEmpty()) {
                    assets[position] = asset.copy(amount = newAmount)
                    assetAdapter.notifyItemChanged(position)
                    updatePortfolioToml()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun updatePortfolioToml() {
        val file = File(requireContext().filesDir, "portfolio.toml")
        val groupedAssets = assets.groupBy { it.category }

        FileWriter(file).use { writer ->
            groupedAssets.forEach { (category, assetList) ->
                writer.write("[$category]\n")
                assetList.forEach { asset ->
                    val amount = asset.amount.toDoubleOrNull()
                    if (amount != null) {
                        writer.write("${asset.symbol} = $amount\n")
                    } else {
                        writer.write("${asset.symbol} = \"${asset.amount}\"\n")
                    }
                }
                writer.write("\n")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webSocket?.close(1000, "Fragment destroyed")
        _binding = null
    }
}