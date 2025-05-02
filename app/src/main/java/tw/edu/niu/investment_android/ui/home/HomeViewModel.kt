package tw.edu.niu.investment_android.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "請至側欄輸入您的資產標的與數量"
    }
    val text: LiveData<String> = _text
}