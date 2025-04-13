package tw.edu.niu.investment_android.ui.outputs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import tw.edu.niu.investment_android.databinding.FragmentOutputsBinding
import java.io.File

class OutputsFragment : Fragment() {

    private var _binding: FragmentOutputsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val outputsViewModel =
            ViewModelProvider(this).get(OutputsViewModel::class.java)

        _binding = FragmentOutputsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 設置 RecyclerView
        binding.recyclerViewOutputs.layoutManager = LinearLayoutManager(requireContext())
        val assets = loadAssetsFromToml()
        binding.recyclerViewOutputs.adapter = AssetAdapter(assets)

        return root
    }

    private fun loadAssetsFromToml(): List<Asset> {
        val assets = mutableListOf<Asset>()
        val file = File(requireContext().filesDir, "portfolio.toml")

        if (file.exists()) {
            var currentCategory = ""
            file.readLines().forEach { line ->
                if (line.startsWith("[")) {
                    currentCategory = line.trim().removeSurrounding("[", "]")
                } else if (line.contains("=")) {
                    val (symbol, amount) = line.split("=").map { it.trim() }
                    // 移除 TOML 中可能的引號（例如 "2330.TW"）
                    val cleanSymbol = symbol.removeSurrounding("\"")
                    assets.add(Asset(currentCategory, cleanSymbol, amount))
                }
            }
        }

        return assets
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}