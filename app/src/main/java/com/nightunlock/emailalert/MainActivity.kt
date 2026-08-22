package com.nightunlock.emailalert

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nightunlock.emailalert.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadPrefs()
        updateStatus()

        binding.btnSave.setOnClickListener { saveAndStart() }
        binding.btnStop.setOnClickListener { stopMonitoring() }
        binding.btnTest.setOnClickListener { sendTestEmail() }

        requestPermissionsIfNeeded()
    }

    private fun loadPrefs() {
        binding.etRecipient.setText(Prefs.getRecipient(this))
        binding.etSender.setText(Prefs.getSender(this))
        binding.etPassword.setText(Prefs.getPassword(this))
    }

    private fun updateStatus() {
        val enabled = Prefs.isEnabled(this)
        binding.tvStatus.text = if (enabled) "Статус: мониторинг активен 🟢" else "Статус: не запущен 🔴"
    }

    private fun saveAndStart() {
        val recipient = binding.etRecipient.text?.toString()?.trim() ?: ""
        val sender = binding.etSender.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString() ?: ""

        if (recipient.isEmpty() || sender.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        Prefs.save(this, recipient, sender, password, true)

        // The app intentionally does not request a battery-optimization exemption.
        // Keeping permissions minimal reduces unnecessary security/antivirus heuristics.
        val intent = Intent(this, MonitorService::class.java)
        ContextCompat.startForegroundService(this, intent)

        updateStatus()
        Toast.makeText(this, "Мониторинг запущен", Toast.LENGTH_SHORT).show()
    }

    private fun stopMonitoring() {
        Prefs.setEnabled(this, false)
        stopService(Intent(this, MonitorService::class.java))
        updateStatus()
        Toast.makeText(this, "Мониторинг остановлен", Toast.LENGTH_SHORT).show()
    }

    private fun sendTestEmail() {
        val recipient = binding.etRecipient.text?.toString()?.trim() ?: ""
        val sender = binding.etSender.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString() ?: ""

        if (recipient.isEmpty() || sender.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Отправка тестового письма...", Toast.LENGTH_SHORT).show()

        Thread {
            val success = EmailSender.send(
                recipient, sender, password,
                "Тест Night Unlock Alert",
                "Это тестовое письмо. Если вы его получили — настройка SMTP работает корректно."
            )
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "Письмо успешно отправлено!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Ошибка отправки. Проверьте логин/пароль приложения.", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun requestPermissionsIfNeeded() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 100)
        }
    }
}
