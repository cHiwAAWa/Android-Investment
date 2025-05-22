package tw.edu.niu.investment_android.ui.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import tw.edu.niu.investment_android.databinding.FragmentSlideshowBinding
import java.util.concurrent.TimeUnit

class SlideshowFragment : Fragment() {
    private var _binding: FragmentSlideshowBinding? = null
    private val binding get() = _binding!!
    private var webSocket: WebSocket? = null
    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // 無限等待，適合串流
        .build()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

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
                    binding.textSlideshow.text = "WebSocket 已連線"
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                requireActivity().runOnUiThread {
                    binding.textSlideshow.text = "收到訊息：$text"
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                requireActivity().runOnUiThread {
                    binding.textSlideshow.text = "收到二進制訊息：${bytes.hex()}"
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                requireActivity().runOnUiThread {
                    binding.textSlideshow.text = "WebSocket 關閉：$reason"
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                requireActivity().runOnUiThread {
                    binding.textSlideshow.text = "WebSocket 錯誤：${t.message}"
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webSocket?.close(1000, "Fragment destroyed")
        _binding = null
    }
}