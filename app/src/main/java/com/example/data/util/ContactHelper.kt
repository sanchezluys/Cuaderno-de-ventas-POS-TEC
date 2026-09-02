package com.example.data.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.result.contract.ActivityResultContract

data class ContactInfo(
    val name: String,
    val phone: String?
)

class PickPhoneContactContract : ActivityResultContract<Void?, Uri?>() {
    override fun createIntent(context: Context, input: Void?): Intent {
        return Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (resultCode == Activity.RESULT_OK) intent?.data else null
    }
}

object ContactHelper {

    fun getContactDetails(context: Context, contactUri: Uri): ContactInfo {
        var name = ""
        var phone: String? = null

        try {
            val contentResolver = context.contentResolver

            // Query the picked phone Uri directly. The system picker grants transient read
            // URI permission to this specific record, requiring no runtime READ_CONTACTS permission.
            contentResolver.query(
                contactUri,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                    if (nameIndex != -1) {
                        name = cursor.getString(nameIndex) ?: ""
                    }
                    if (numberIndex != -1) {
                        phone = cursor.getString(numberIndex)?.replace(" ", "")?.replace("-", "")
                    }
                }
            }

            // Fallback for general contact URIs if name is still empty
            if (name.isBlank()) {
                contentResolver.query(
                    contactUri,
                    arrayOf(ContactsContract.Contacts.DISPLAY_NAME),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            name = cursor.getString(nameIndex) ?: ""
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (name.isBlank()) {
            name = "Contacto"
        }

        return ContactInfo(name = name, phone = phone)
    }
}
