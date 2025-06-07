package tw.edu.niu.investment_android.ui.inputs

import android.R
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import tw.edu.niu.investment_android.databinding.FragmentInputsBinding
import java.io.File
import java.io.FileWriter
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import java.util.Locale

class InputsFragment : Fragment() {

    private var _binding: FragmentInputsBinding? = null
    private val binding get() = _binding!!
    private val okHttpClient = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inputsViewModel =
            ViewModelProvider(this).get(InputsViewModel::class.java)

        _binding = FragmentInputsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 設定 Spinner（下拉選單）
        val options = listOf("US Stock", "US ETF", "TW Stock", "TW ETF", "Crypto")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.inputSpinner?.adapter = adapter

        // 設定「確認」按鈕點擊事件
        binding.btnAdd.setOnClickListener {
            val category = binding.inputSpinner.selectedItem.toString()
            val symbol = binding.inputAssetName.text.toString().trim()
            val amountStr = binding.inputAmount.text.toString().trim()

            // 驗證輸入
            if (symbol.isEmpty()) {
                Toast.makeText(requireContext(), "請輸入標的名稱", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (amountStr.isEmpty()) {
                Toast.makeText(requireContext(), "請輸入數量", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 驗證數量是否為有效的數字（支援小數）
            val amount = amountStr.toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(
                    requireContext(),
                    "數量必須是有效的數字（支援小數，如 0.5）",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // 將輸入資料轉換為指定格式並儲存
            saveToFile(category, symbol, amountStr)
        }

        return root
    }

    private fun saveToFile(category: String, symbol: String, amount: String) {
        try {
            // 將分類轉換為 TOML 中的表名
            val categoryHeader = when (category) {
                "US Stock" -> "us-stock"
                "US ETF" -> "us-etf"
                "TW Stock" -> "tw-stock"
                "TW ETF" -> "tw-etf"
                "Crypto" -> "crypto"
                else -> return
            }

            // 定義檔案路徑
            val file = File(requireContext().filesDir, "portfolio.toml")
            val data = mutableMapOf<String, MutableList<Pair<String, String>>>()

            // 如果檔案存在，讀取現有資料，並將資產名稱轉為大寫
            if (file.exists()) {
                file.readLines().forEach { line ->
                    if (line.startsWith("[")) {
                        val currentHeader = line.trim().removeSurrounding("[", "]")
                        data[currentHeader] = mutableListOf()
                    } else if (line.contains("=")) {
                        val (sym, amt) = line.split("=").map { it.trim() }
                        val cleanSym = sym.removeSurrounding("\"").lowercase(Locale.getDefault()) // 轉為小寫
                        data[data.keys.last()]?.add(cleanSym to amt)
                    }
                }
            }

            // 將新輸入的資產名稱轉為小寫
            val lowerSymbol = symbol.lowercase(Locale.getDefault())

            // 添加新資料
            if (!data.containsKey(categoryHeader)) {
                data[categoryHeader] = mutableListOf()
            }
            val existingEntry = data[categoryHeader]?.indexOfFirst { it.first == lowerSymbol }
            if (existingEntry != null && existingEntry >= 0) {
                // 更新現有資產的數量：將新舊數量相加
                val oldAmount = data[categoryHeader]?.get(existingEntry)?.second?.toDoubleOrNull() ?: 0.0
                val newAmount = amount.toDoubleOrNull() ?: 0.0
                val totalAmount = (oldAmount + newAmount).toString()
                data[categoryHeader]?.set(existingEntry, lowerSymbol to totalAmount)
            } else {
                // 添加新資產
                data[categoryHeader]?.add(lowerSymbol to amount)
            }

            // 寫入檔案（TOML 格式）
            FileWriter(file).use { writer ->
                data.forEach { (header, entries) ->
                    writer.write("[$header]\n")
                    entries.forEach { (sym, amt) ->
                        // TOML 中數字不需要引號，檢查 amount 是否為數字
                        if (amt.toDoubleOrNull() != null) {
                            writer.write("$sym = $amt\n")
                        } else {
                            writer.write("$sym = \"$amt\"\n")
                        }
                    }
                    writer.write("\n")
                }
            }

            Toast.makeText(requireContext(), "資料已儲存到 portfolio.toml", Toast.LENGTH_SHORT).show()
            // 清空輸入欄位
            binding.inputAssetName.text.clear()
            binding.inputAmount.text.clear()

            // 上傳檔案到服務器
            uploadFileToServer(file)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "儲存失敗: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadFileToServer(file: File) {
        // 假設 Tailscale VM IP 為 100.79.72.71
        val serverUrl = "http://100.79.72.71:3030/upload"
        val mediaType = "application/toml".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, file)

        val request = Request.Builder()
            .url(serverUrl)
            .post(requestBody)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "上傳失敗: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                requireActivity().runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "檔案上傳成功", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "上傳失敗: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}