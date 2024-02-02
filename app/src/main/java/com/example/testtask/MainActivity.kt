package com.example.testtask

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import com.example.testtask.ui.theme.TestTaskTheme
import java.util.Arrays
import java.util.Locale
import java.util.zip.GZIPInputStream
import kotlin.text.Charsets.UTF_8


class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private var isDialogShow = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getDataFromAssets(this)

        setContent {

            val text = viewModel.getData.collectAsState(initial = "")

            val arr = text.value.split("\n")

            val list = mutableListOf<String>()
            arr.forEach { it ->
                if (it.contains("01 03") || it.contains("01 10")) {
                    Log.d(
                        "TTLog", it.drop(24).trim()
                            .substringBefore("/")
                    )

                    convertToBits(it.drop(24).trim()
                        .substringBefore("/").filterNot { s -> s.isWhitespace() })

                    list.add(it)
                }
            }

            val showDialog = remember { isDialogShow }
            if (showDialog.value) {
                ShowErrorDialog(
                    msg = "Файл более 500 мб",
                    showDialog = true,
                    onDismiss = { isDialogShow.value = false })
            }

            TestTaskTheme {
                Scaffold(
                    floatingActionButton = {
                        SetFAB {
                            openFile()
                        }
                    },
                    content = { padding ->
                        Surface(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = text.value,
                                fontSize = 16.sp,
                            )
                        }
                    }
                )
            }
        }
    }

    private fun convertToBits(byteString: String) {
        if (byteString.contains("0103")) {

            val hexString = byteString.drop(6).dropLast(4)
//            var bytearray: ByteArray = BigInteger(hexString, 16).toByteArray()

            val bytearray = hexString.decodeHex()
            val bitset = ArrayList<String>()
            Log.d("TTLog", hexString)
            Log.d("TTLog", "${bytearray.size}, ${Arrays.toString(bytearray)}")

            for (i in bytearray) {
                bitset.add(i.toString(2))
                Log.d("TTLog", i.toString(2))
            }

        }

    }

    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        val mimeTypes = arrayOf(
            "text/plain",
            "text/x-log",
            "application/octet-stream",
            "text/csv",
            "application/msword",
            "application/gzip",
            "application/pdf",
            "application/xml",
            "text/html"
        )
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        resultLauncher.launch(intent)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                result.data?.data?.let {
                    if (getFileSize(it) > MAX_MB) {
                        isDialogShow.value = true
                    } else {
                        viewModel.getDataFromFile(URIPathHelper.getFile(this, it).absolutePath)
                    }
                }
            }
        }

    fun ungzip(content: ByteArray): String =
        GZIPInputStream(content.inputStream()).bufferedReader(UTF_8).use { it.readText() }


    private fun getFileSize(uri: Uri): Long {
        return DocumentFile.fromSingleUri(this, uri)?.length() ?: 0
    }

    fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }

        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    fun bytes2HexStr(src: ByteArray?): String {
        val builder = java.lang.StringBuilder()
        if (src == null || src.size <= 0) {
            return ""
        }
        val buffer = CharArray(2)
        for (i in src.indices) {
            buffer[0] = Character.forDigit(src[i].toInt() ushr 4 and 0x0F, 16)
            buffer[1] = Character.forDigit(src[i].toInt() and 0x0F, 16)
            builder.append(buffer)
        }
        return builder.toString().uppercase(Locale.getDefault())
    }

    fun ByteArray.bytesToHexString(
        spaces: Boolean = false
    ): String {
        val format = if (spaces) "%02x " else "%02x"
        val sb = StringBuilder()
        for (i in 0 until size) {
            sb.append(format.format(this[i]))
        }
        return sb.toString()
    }

    @Composable
    fun ShowErrorDialog(
        msg: String,
        showDialog: Boolean,
        onDismiss: () -> Unit
    ) {
        if (showDialog) {
            AlertDialog(
                title = {
                    Text(msg)
                },
                onDismissRequest = onDismiss,
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Закрыть")
                    }
                },
                dismissButton = {}
            )
        }
    }

    companion object {
        const val MAX_MB = 524288000L
    }

}

@Composable
fun ShowText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
    )
}

@Composable
fun SetFAB(onClick: () -> Unit) {
    SmallFloatingActionButton(
        onClick = { onClick() },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.secondary
    ) {
        Icon(Icons.Filled.Add, "Small floating action button.")
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    TestTaskTheme {
        ShowText("SomeText")
    }
}
