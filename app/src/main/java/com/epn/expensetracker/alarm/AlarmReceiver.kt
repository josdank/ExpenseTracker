package com.epn.expensetracker.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.epn.expensetracker.MainActivity
import com.epn.expensetracker.R

/**
 * BroadcastReceiver que se ejecuta cuando la alarma se dispara.
 * Funciona incluso con la app completamente cerrada.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        mostrarNotificacion(context)

        // Reprogramar para el día siguiente
        val hora = ReminderPreferences.obtenerHora(context)
        val minuto = ReminderPreferences.obtenerMinuto(context)
        ReminderScheduler.programarRecordatorio(context, hora, minuto)
    }

    private fun mostrarNotificacion(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear el canal (obligatorio desde Android 8.0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = Settings.System.DEFAULT_NOTIFICATION_URI
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val canal = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorios diarios de gastos"

                // Vibración
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)

                // Sonido
                setSound(soundUri, audioAttributes)
            }

            notificationManager.createNotificationChannel(canal)
        }

        // Intent para abrir la app al tocar la notificación
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("¿Registraste tus gastos?")
            .setContentText("No olvides anotar lo que gastaste hoy")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            // En pre-O ayuda; en O+ manda el canal
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notificacion)
    }

    companion object {
        const val CHANNEL_ID = "expense_reminder_channel"
        const val NOTIFICATION_ID = 1001
    }
}
