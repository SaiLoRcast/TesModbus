package com.example.testtask

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.InputStream

class MainViewModel : ViewModel() {

    private val _getData = MutableStateFlow(String())
    val getData: StateFlow<String> = _getData.asStateFlow()

    fun getDataFromAssets(context: Context) {
        viewModelScope.launch {
            _getData.emit(loadFileFromAsset(context) ?: "")
        }
    }

    fun getDataFromFile(fileName: String) {
        viewModelScope.launch {
            val text = File(fileName).readText()
            _getData.emit(text)
        }
    }

    private fun loadFileFromAsset(context: Context): String? {
        val json: String? = try {
            val inputStream: InputStream = context.assets.open("demo.log")
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, charset("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

}