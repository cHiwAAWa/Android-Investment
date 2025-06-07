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
import java.util.Locale

class OutputsFragment : Fragment() {
    private var _binding: FragmentOutputsBinding? = null
    private val binding get() = _binding!!
    private lateinit var assetAdapter: AssetAdapter
    private val assets = mutableListOf<Asset>()

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

        return root
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
                    val cleanSymbol = symbol.removeSurrounding("\"").lowercase(Locale.getDefault()) // 轉為小寫
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
        _binding = null
    }
}