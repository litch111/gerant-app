package com.machawi.gerant

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {

    // CHANGE THIS to your actual site's owner/gerant dashboard URL.
    private val gerantUrl = "https://machawi-chez-khriji.vercel.app/gerant"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WebView.setWebContentsDebuggingEnabled(true)

        val webView = WebView(this)
        setContentView(webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()

        // Required for things like the "Cloturer la journee ?" confirmation
        // popup to actually show up. Without a WebChromeClient, a WebView
        // silently ignores JavaScript's confirm()/alert() calls entirely —
        // the button looks like it does nothing, when really it's just
        // stuck waiting on a dialog that was never shown.
        webView.webChromeClient = WebChromeClient()

        webView.loadUrl(gerantUrl)
    }
}
