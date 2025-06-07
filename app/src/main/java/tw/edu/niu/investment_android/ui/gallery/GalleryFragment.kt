package tw.edu.niu.investment_android.ui.gallery

import android.os.Bundle
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
import kotlin.math.max

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()
    private var webSocket: WebSocket? = null
    private val priceData = mutableMapOf<String, MutableList<Double>>()
    private val volatilities = mutableListOf<Volatility>()
    private lateinit var volatilityAdapter: VolatilityAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 設置 RecyclerView
        binding.recyclerViewVolatility.layoutManager = LinearLayoutManager(requireContext())
        volatilityAdapter = VolatilityAdapter(volatilities)
        binding.recyclerViewVolatility.adapter = volatilityAdapter

        // 從 portfolio.toml 載入標的
        loadSymbolsFromToml()

        // 連接到 WebSocket
        connectWebSocket()

        // 每分鐘計算波動率
        startVolatilityCalculation()

        return root
    }

    private fun loadSymbolsFromToml() {
        val file = File(requireContext().filesDir, "portfolio.toml")
        if (file.exists()) {
            var currentCategory = ""
            file.readLines().forEach { line ->
                try {
                    if (line.trim().startsWith("[")) {
                        currentCategory = line.trim().removeSurrounding("[", "]")
                    } else if (line.contains("=")) {
                        val (symbol, _) = line.split("=").map { it.trim() }
                        val cleanSymbol = symbol.removeSurrounding("\"").lowercase(Locale.getDefault())
                        if (!priceData.containsKey(cleanSymbol)) {
                            priceData[cleanSymbol] = mutableListOf()
                        }
                    }
                } catch (e: Exception) {
                    // 忽略無效行
                }
            }
            binding.tvEmpty.visibility = if (priceData.isEmpty()) View.VISIBLE else View.GONE
        } else {
            binding.tvEmpty.visibility = View.VISIBLE
        }
    }

    private fun connectWebSocket() {
        val wsUrl = "ws://100.79.72.71:3030/ws"
        val request = Request.Builder().url(wsUrl).build()
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                requireActivity().runOnUiThread {
                    binding.tvStatus.text = "WebSocket 已連線"
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                requireActivity().runOnUiThread {
                    binding.tvStatus.text = "收到訊息"
                    try {
                        var currentCategory = ""
                        text.lines().forEach { line ->
                            if (line.trim().startsWith("[")) {
                                currentCategory = line.trim().removeSurrounding("[", "]")
                            } else if (line.contains("=")) {
                                val (symbol, priceStr) = line.split("=").map { it.trim() }
                                val cleanSymbol = symbol.removeSurrounding("\"").lowercase(Locale.getDefault())
                                val price = priceStr.toDoubleOrNull()
                                if (price != null && priceData.containsKey(cleanSymbol)) {
                                    priceData[cleanSymbol]?.add(price)
                                    if (priceData[cleanSymbol]?.size ?: 0 > 60) {
                                        priceData[cleanSymbol]?.removeAt(0)
                                    }
                                }
                            }
                        }
                        calculateVolatilities()
                    } catch (e: Exception) {
                        binding.tvStatus.text = "解析錯誤：${e.message}"
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                requireActivity().runOnUiThread {
                    binding.tvStatus.text = "WebSocket 錯誤：${t.message}"
                }
            }
        })
    }

    private fun startVolatilityCalculation() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                calculateVolatilities()
                handler.postDelayed(this, 60000)
            }
        }, 1000)
    }

    private fun calculateVolatilities() {
        volatilities.clear()
        priceData.forEach { (symbol, prices) ->
            if (prices.isNotEmpty()) {
                val currentPrice = prices.last()
                val maxPrice = prices.maxOrNull() ?: currentPrice
                val minPrice = prices.minOrNull() ?: currentPrice
                val volatility = if (currentPrice != 0.0) {
                    max(
                        abs(currentPrice - maxPrice),
                        abs(currentPrice - minPrice)
                    ) / currentPrice * 100
                } else 0.0
                volatilities.add(Volatility(symbol, volatility))
            }
        }
        requireActivity().runOnUiThread {
            binding.tvEmpty.visibility = if (volatilities.isEmpty()) View.VISIBLE else View.GONE
            volatilityAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webSocket?.close(1000, "Fragment destroyed")
        _binding = null
    }
}