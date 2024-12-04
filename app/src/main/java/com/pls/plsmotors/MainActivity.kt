package com.pls.plsmotors

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.pls.plsmotors.ui.theme.PLSMotorsTheme

class MainActivity : ComponentActivity() {
    private var filePathCallback: android.webkit.ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )

        setContent {
            PLSMotorsTheme {
                WebViewScreen("https://pepelopez79.github.io/PLSMotors/")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            val resultUris: ArrayList<Uri>? = data?.clipData?.let { clipData ->
                ArrayList<Uri>().apply {
                    for (i in 0 until clipData.itemCount) {
                        add(clipData.getItemAt(i).uri)
                    }
                }
            } ?: data?.data?.let { uri ->
                arrayListOf(uri)
            }

            if (!resultUris.isNullOrEmpty()) {
                filePathCallback?.onReceiveValue(resultUris.toArray(arrayOf()))
            } else {
                filePathCallback?.onReceiveValue(emptyArray())
            }

            filePathCallback = null
        }
    }

    @Composable
    fun WebViewScreen(url: String) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            super.onReceivedError(view, errorCode, description, failingUrl)
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onJsAlert(
                            view: WebView?,
                            url: String?,
                            message: String?,
                            result: android.webkit.JsResult?
                        ): Boolean {
                            val builder = androidx.appcompat.app.AlertDialog.Builder(view?.context ?: return false)
                            builder.setTitle("PLSMotors")
                            builder.setMessage(message)
                            builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
                                result?.confirm()
                                dialog.dismiss()
                            }
                            builder.setCancelable(false)
                            builder.create().show()
                            return true
                        }

                        override fun onShowFileChooser(
                            view: WebView?,
                            filePathCallback: android.webkit.ValueCallback<Array<Uri>>?,
                            fileChooserParams: FileChooserParams?
                        ): Boolean {
                            this@MainActivity.filePathCallback = filePathCallback

                            val intent = Intent(Intent.ACTION_GET_CONTENT)
                            intent.type = "image/*"
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                            (view?.context as Activity).startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE)

                            return true
                        }
                    }

                    loadUrl(url)
                }
            }
        )
    }

    companion object {
        private const val FILE_CHOOSER_REQUEST_CODE = 1
    }
}
