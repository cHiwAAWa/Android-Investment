package tw.edu.niu.investment_android.ui.inputs

import android.R
import android.os.Bundle
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

class InputsFragment : Fragment() {

    private var _binding: FragmentInputsBinding? = null
    private val binding get() = _binding!!

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
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.inputSpinner.adapter = adapter

        // 設定「確認」按鈕點擊事件
        binding.btnAdd.setOnClickListener {
            val category = binding.inputSpinner.selectedItem.toString()
            val symbol = binding.inputAssetName.text.toString().trim()
            val amount = binding.inputAmount.text.toString().trim()

            // 驗證輸入
            if (symbol.isEmpty() || amount.isEmpty()) {
                Toast.makeText(requireContext(), "請輸入標的名稱和數量", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 將輸入資料轉換為指定格式並儲存
            saveToFile(category, symbol, amount)
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

            // 定義檔案路徑（使用 portfolio.toml）
            val file = File(requireContext().filesDir, "portfolio.toml")
            val data = mutableMapOf<String, MutableList<Pair<String, String>>>()

            // 如果檔案存在，讀取現有資料
            if (file.exists()) {
                file.readLines().forEach { line ->
                    if (line.startsWith("[")) {
                        val currentHeader = line.trim().removeSurrounding("[", "]")
                        data[currentHeader] = mutableListOf()
                    } else if (line.contains("=")) {
                        val (sym, amt) = line.split("=").map { it.trim() }
                        // TOML 要求數字格式（不加引號），檢查 amount 是否為數字
                        data[data.keys.last()]?.add(sym to amt)
                    }
                }
            }

            // 添加新資料
            if (!data.containsKey(categoryHeader)) {
                data[categoryHeader] = mutableListOf()
            }
            // 檢查是否已存在相同的 symbol，若存在則更新數量
            val existingEntry = data[categoryHeader]?.indexOfFirst { it.first == symbol }
            if (existingEntry != null && existingEntry >= 0) {
                data[categoryHeader]?.set(existingEntry, symbol to amount)
            } else {
                data[categoryHeader]?.add(symbol to amount)
            }

            // 寫入檔案（TOML 格式）
            FileWriter(file).use { writer ->
                data.forEach { (header, entries) ->
                    writer.write("[$header]\n")
                    entries.forEach { (sym, amt) ->
                        // TOML 中數字不需要引號，檢查是否為數字
                        if (amt.toDoubleOrNull() != null) {
                            writer.write("$sym = $amt\n")
                        } else {
                            writer.write("$sym = \"$amt\"\n") // 如果不是數字，加引號
                        }
                    }
                    writer.write("\n")
                }
            }

            Toast.makeText(requireContext(), "資料已儲存到 portfolio.toml", Toast.LENGTH_SHORT).show()
            // 清空輸入欄位
            binding.inputAssetName.text.clear()
            binding.inputAmount.text.clear()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "儲存失敗: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}