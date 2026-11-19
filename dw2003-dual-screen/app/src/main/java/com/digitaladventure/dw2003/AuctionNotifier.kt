package com.digitaladventure.dw2003

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.digitaladventure.dw2003.model.GameSnapshot

class AuctionNotifier(private val context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun maybeNotify(snapshot: GameSnapshot, spanish: Boolean) {
        val notice = snapshot.auction?.takeIf { snapshot.isLive && snapshot.gameStarted } ?: return
        val seen = prefs.getStringSet(KEY_SEEN, emptySet()).orEmpty()
        if (notice.id in seen) return
        ensureChannel()
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val launch = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle(
                if (spanish) "Subasta en El Dorado" else "El Dorado auction"
            )
            .setContentText(notice.label(spanish))
            .setStyle(NotificationCompat.BigTextStyle().bigText(notice.label(spanish)))
            .setContentIntent(launch)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        prefs.edit().putStringSet(KEY_SEEN, seen + notice.id).apply()
    }

    fun shouldRequestPermission(snapshot: GameSnapshot): Boolean {
        if (Build.VERSION.SDK_INT < 33) return false
        if (!snapshot.wantsAlertPermission() || !snapshot.isLive || !snapshot.gameStarted) return false
        if (prefs.getBoolean(KEY_ASKED, false)) return false
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun markPermissionAsked() {
        prefs.edit().putBoolean(KEY_ASKED, true).apply()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < 26) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "El Dorado",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos cuando Flawe llega a una subasta de El Dorado."
            }
        )
    }

    companion object {
        private const val PREFS = "dw2003_auctions"
        private const val KEY_SEEN = "seen_ids"
        private const val KEY_ASKED = "permission_asked"
        private const val CHANNEL_ID = "eldorado_auctions"
        private const val NOTIFICATION_ID = 37604
    }
}
