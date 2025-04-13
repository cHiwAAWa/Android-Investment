package tw.edu.niu.investment_android.ui.outputs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import tw.edu.niu.investment_android.databinding.FragmentOutputsBinding
import tw.edu.niu.investment_android.databinding.FragmentSlideshowBinding
import tw.edu.niu.investment_android.ui.outputs.OutputsViewModel

class OutputsFragment : Fragment() {

    private var _binding: FragmentOutputsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
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

        val textView: TextView = binding.textOutputs
        outputsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}