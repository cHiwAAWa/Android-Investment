package tw.edu.niu.investment_android.ui.outputs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OutputsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is outputs Fragment"
    }
    val text: LiveData<String> = _text
}