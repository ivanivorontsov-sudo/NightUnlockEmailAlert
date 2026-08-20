package com.nightunlock.emailalert

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val NAME = "night_unlock_prefs"
    private const val KEY_RECIPIENT = "recipient"
    private const val KEY_SENDER = "sender"
    private const val KEY_PASSWORD = "password"
    private const val KEY_ENABLED = "enabled"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun getRecipient(context: Context): String =
        prefs(context).getString(KEY_RECIPIENT, "") ?: ""

    fun getSender(context: Context): String =
        prefs(context).getString(KEY_SENDER, "") ?: ""

    fun getPassword(context: Context): String =
        prefs(context).getString(KEY_PASSWORD, "") ?: ""

    fun isEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ENABLED, false)

    fun save(context: Context, recipient: String, sender: String, password: String, enabled: Boolean) {
        prefs(context).edit()
            .putString(KEY_RECIPIENT, recipient.trim())
            .putString(KEY_SENDER, sender.trim())
            .putString(KEY_PASSWORD, password)
            .putBoolean(KEY_ENABLED, enabled)
            .apply()
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }
}
