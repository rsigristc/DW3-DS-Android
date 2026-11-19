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

class DriNotifier(private val context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun maybeNotify(snapshot: GameSnapshot, spanish: Boolean) {
        if (!snapshot.isLive || !snapshot.gameStarted) return
        val ready = snapshot.dri.filter { it.ready }
        if (ready.isEmpty()) return
        val seen = prefs.getStringSet(KEY_SEEN, emptySet()).orEmpty()
        val fresh = ready.filter { it.id !in seen }
        if (fresh.isEmpty()) return
        ensureChannel()
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val launch = PendingIntent.getActivity(
            context,
            1,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val body = fresh.joinToString("\n") { it.label(spanish) }
        val title = if (spanish) {
            if (fresh.size == 1) "DRI listo: ${fresh.first().rookie}" else "Agentes DRI listos"
        } else {
            if (fresh.size == 1) "DRI ready: ${fresh.first().rookie}" else "DRI agents ready"
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(launch)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        prefs.edit().putStringSet(KEY_SEEN, seen + fresh.map { it.id }).apply()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < 26) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "DRI",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos cuando un agente DRI acepta el nivel de tu equipo."
            }
        )
    }

    companion object {
        private const val PREFS = "dw2003_dri"
        private const val KEY_SEEN = "seen_ids"
        private const val CHANNEL_ID = "dri_agents"
        private const val NOTIFICATION_ID = 37605
    }
}
