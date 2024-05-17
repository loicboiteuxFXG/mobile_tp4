package com.example.mapssages

import android.app.Application
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import android.content.ContentResolver;

class SMSBroadcastReceiver : android.content.BroadcastReceiver() {


    val SMS_URI_INBOX = "content://sms/inbox"

    override fun onReceive(context: Context, intent: Intent) {
        // Ici, on utilise un bloc `when` pour gérer les différentes actions reçues.
        when (intent.action) {
            // Réagit à la réception d'un SMS.
            Telephony.Sms.Intents.SMS_RECEIVED_ACTION -> {
                Log.d("SMSBroadcastReceiver", "SMS received")
            }
        }
    }
}
