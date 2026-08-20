package com.nightunlock.emailalert

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

class UnlockReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "UnlockReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_USER_PRESENT -> {
                Log.d(TAG, "USER_PRESENT received")
                if (Prefs.isEnabled(context) && isNightTime()) {
                    sendAlert(context)
                }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d(TAG, "BOOT_COMPLETED")
                if (Prefs.isEnabled(context)) {
                    // Restart service after reboot
                    val serviceIntent = Intent(context, MonitorService::class.java)
                    context.startForegroundService(serviceIntent)
                }
            }
        }
    }

    private fun isNightTime(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        // 00:00 inclusive to 07:00 exclusive
        return hour in 0 until 7
    }

    private fun sendAlert(context: Context) {
        val recipient = Prefs.getRecipient(context)
        val sender = Prefs.getSender(context)
        val password = Prefs.getPassword(context)

        if (recipient.isBlank() || sender.isBlank() || password.isBlank()) {
            Log.w(TAG, "Credentials not configured")
            return
        }

        Thread {
            val now = Calendar.getInstance()
            val timeStr = String.format(
                "%02d:%02d:%02d",
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                now.get(Calendar.SECOND)
            )
            val subject = "⚠️ Телефон разблокирован ночью!"
            val body = "Устройство было разблокировано в $timeStr (локальное время).\n\nЭто автоматическое уведомление от Night Unlock Email Alert."

            val success = EmailSender.send(recipient, sender, password, subject, body)
            Log.i(TAG, "Alert email sent: $success")
        }.start()
    }
}
