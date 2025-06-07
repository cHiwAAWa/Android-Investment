package tw.edu.niu.investment_android.ui.gallery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import tw.edu.niu.investment_android.databinding.FragmentGalleryBinding
import okhttp3.*
import okio.ByteString
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.Locale
import kotlin.math.abs
import tw.edu.niu.investment_android.ui.gallery.Volatility
import tw.edu.niu.investment_android.ui.gallery.VolatilityAdapter

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private val TAG = "GalleryFragment"

    // symbol -> recent prices
    private val priceData: MutableMap<String, MutableList<Double>> = mutableMapOf()
    // 改為存放 Volatility 物件，而非 Pair
    private val volatilities: MutableList<Volatility> = mutableListOf()
    private lateinit var volatilityAdapter: VolatilityAdapter

    private var webSocket: WebSocket? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化 RecyclerView：注意這裡用 binding.recyclerViewVolatility 或你 xml 中的 id
        volatilityAdapter = VolatilityAdapter(volatilities)
        binding.recyclerViewVolatility.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = volatilityAdapter
        }

        loadSymbolsFromToml()
        updateEmptyView()
        connectWebSocket()
    }

    private fun connectWebSocket() {
        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()
        val request = Request.Builder()
            .url("ws://100.79.72.71:3030/ws")
            .build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                requireActivity().runOnUiThread {
                    binding.tvStatus.text = "連線已開啟"
                    Log.d(TAG, "WebSocket opened")
                }
            }

            override fun onMessage(ws: WebSocket, text: String) {
                requireActivity().runOnUiThread {
                    Log.d(TAG, "Raw message: $text")
                    binding.tvStatus.text = "收到訊息：$text"
                    text.lines().forEach { line ->
                        val trimmed = line.trim()
                        if (trimmed.contains(":")) {
                            val (sym, pricePart) = trimmed.split(":").map { it.trim() }
                            val symbol = sym.lowercase(Locale.getDefault())
                            val price = pricePart.removePrefix("$").toDoubleOrNull()
                            if (price != null && priceData.containsKey(symbol)) {
                                priceData[symbol]!!.apply {
                                    add(price)
                                    if (size > 60) removeAt(0)
                                }
                                Log.d(TAG, "Updated price for $symbol: $price")
                            } else {
                                Log.w(TAG, "Invalid price or unknown symbol: $trimmed")
                            }
                        }
                    }
                    calculateVolatilities()
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                requireActivity().runOnUiThread {
                    binding.tvStatus.text = "連線失敗：${t.message}"
                    Log.e(TAG, "WebSocket failure", t)
                }
            }
        })
    }

    private fun loadSymbolsFromToml() {
        try {
            val file = File(requireContext().filesDir, "portfolio.toml")
            if (!file.exists()) {
                Log.w(TAG, "portfolio.toml not found")
                return
            }
            file.forEachLine { line ->
                val trimmed = line.trim()
                if (trimmed.contains("=") && !trimmed.startsWith("[")) {
                    val symbolRaw = trimmed.split("=").first().trim().removeSurrounding("\"")
                    val symbol = symbolRaw.lowercase(Locale.getDefault())
                    priceData.putIfAbsent(symbol, mutableListOf())
                }
            }
            Log.d(TAG, "Loaded symbols: ${priceData.keys}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load toml", e)
        }
    }

    private fun calculateVolatilities() {
        volatilities.clear()
        priceData.forEach { (symbol, prices) ->
            if (prices.size >= 2) {
                val mean = prices.average()
                val variance = prices.map { (it - mean) * (it - mean) }.average()
                val volatilityValue = abs(kotlin.math.sqrt(variance))
                // 建立 Volatility 物件，而非 Pair
                volatilities.add(Volatility(symbol, volatilityValue))
            }
        }
        volatilities.sortByDescending { it.volatility }
        volatilityAdapter.notifyDataSetChanged()
        updateEmptyView()
    }

    private fun updateEmptyView() {
        binding.tvEmpty.visibility =
            if (volatilities.isEmpty()) View.VISIBLE
            else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webSocket?.close(1000, "Fragment destroyed")
        _binding = null
    }
}
