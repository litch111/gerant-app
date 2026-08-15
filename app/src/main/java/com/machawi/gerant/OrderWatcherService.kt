package com.machawi.gerant

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Runs quietly in the background — even while the tablet is showing a
 * different app — and checks every few seconds whether a new order has
 * come in. When one does, it shows a notification with a sound.
 *
 * How it checks: rather than keeping a full, always-open connection to
 * Firestore (which needs the full Firebase SDK and a signed-in session),
 * this just does a plain, lightweight web request to one small public
 * "something new happened" document — see placeOrder() in firebase.js on
 * the website, which updates that document every time a real customer
 * places an order.
 *
 * CHANGE PROJECT_ID below if it's ever different (it's the same one from
 * firebaseConfig.projectId in src/firebase.js).
 */
class OrderWatcherService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val pollIntervalMs = 8000L
    private lateinit var prefs: SharedPreferences

    private val projectId = "machawi-chez-khriji"
    private val signalUrl =
        "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/state/latestOrderSignal"

    private val pollRunnable = object : Runnable {
        override fun run() {
            Thread { checkForNewOrder() }.start()
            handler.postDelayed(this, pollIntervalMs)
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("order_watcher", Context.MODE_PRIVATE)
        createNotificationChannels()
        startForeground(1, buildPersistentNotification())
        handler.post(pollRunnable)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // START_STICKY: if Android kills this service under memory pressure,
        // it tries to restart it automatically.
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(pollRunnable)
    }

    private fun checkForNewOrder() {
        try {
            val connection = URL(signalUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            val body = connection.inputStream.bufferedReader().readText()
            connection.disconnect()

            val fields = JSONObject(body).optJSONObject("fields") ?: return
            val timestamp = fields.optJSONObject("timestamp")
                ?.optString("integerValue")?.toLongOrNull() ?: return
            val orderNo = fields.optJSONObject("orderNo")?.optString("stringValue") ?: "?"

            val lastSeen = prefs.getLong("last_seen_timestamp", 0L)
            if (timestamp > lastSeen) {
                prefs.edit().putLong("last_seen_timestamp", timestamp).apply()
                // Skip alerting on the very first check after the service
                // starts fresh (e.g. right after install) — only alert on
                // signals that are genuinely new since we started watching.
                if (lastSeen != 0L) {
                    showNewOrderNotification(orderNo)
                }
            }
        } catch (e: Exception) {
            // Network hiccup or similar — just try again next cycle.
        }
    }

    private fun showNewOrderNotification(orderNo: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ALERTS)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        val notification = builder
            .setContentTitle("Nouvelle commande")
            .setContentText("Commande #$orderNo reçue")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setAutoCancel(true)
            .build()
        nm.notify(orderNo.hashCode(), notification)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_STATUS, "Statut", NotificationManager.IMPORTANCE_LOW)
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ALERTS, "Nouvelles commandes", NotificationManager.IMPORTANCE_HIGH)
        )
    }

    private fun buildPersistentNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_STATUS)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        return builder
            .setContentTitle("Machawi Gerant")
            .setContentText("En veille pour les nouvelles commandes")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .build()
    }

    companion object {
        private const val CHANNEL_STATUS = "watcher_status"
        private const val CHANNEL_ALERTS = "new_orders"
    }
}
