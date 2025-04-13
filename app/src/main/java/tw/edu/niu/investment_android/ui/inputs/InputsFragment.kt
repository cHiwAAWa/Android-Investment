package tw.edu.niu.investment_android.ui.inputs

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import tw.edu.niu.investment_android.databinding.FragmentInputsBinding
import tw.edu.niu.investment_android.databinding.FragmentSlideshowBinding
import tw.edu.niu.investment_android.ui.inputs.InputsViewModel

class InputsFragment : Fragment() {

    private var _binding: FragmentInputsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
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

        val textView: TextView = binding.textInputs
        inputsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        // 設定 Spinner（下拉選單）
        val options = listOf("US Stock", "US ETF", "TW Stock", "TW ETF", "Crypto")
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.inputSpinner.adapter = adapter

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}