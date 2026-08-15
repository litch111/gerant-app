package com.machawi.gerant

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {

    // CHANGE THIS to your actual site's owner/gerant dashboard URL.
    private val gerantUrl = "https://your-site.vercel.app/gerant"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WebView.setWebContentsDebuggingEnabled(true)

        startOrderWatcher()

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

    /**
     * Starts the background service that watches for new orders and shows
     * a notification, even while this app isn't the one on screen.
     * On Android 13+, notifications require the user's permission first —
     * this asks for it if not already granted.
     */
    private fun startOrderWatcher() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
        val intent = Intent(this, OrderWatcherService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
