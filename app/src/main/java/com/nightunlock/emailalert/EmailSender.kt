package com.nightunlock.emailalert

import android.util.Log
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailSender {
    private const val TAG = "EmailSender"

    fun send(
        recipient: String,
        sender: String,
        password: String,
        subject: String,
        body: String
    ): Boolean {
        return try {
            val props = Properties().apply {
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.port", "587")
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.ssl.trust", "smtp.gmail.com")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(sender, password)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(sender))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient))
                setSubject(subject)
                setText(body)
            }

            Transport.send(message)
            Log.i(TAG, "Email sent successfully to $recipient")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send email", e)
            false
        }
    }
}
