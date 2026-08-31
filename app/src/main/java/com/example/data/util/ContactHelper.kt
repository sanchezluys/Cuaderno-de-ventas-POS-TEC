package com.example.data.util

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

data class ContactInfo(
    val name: String,
    val phone: String?
)

object ContactHelper {

    fun getContactDetails(context: Context, contactUri: Uri): ContactInfo {
        var name = ""
        var phone: String? = null
        var contactId: String? = null

        try {
            val contentResolver = context.contentResolver
            contentResolver.query(
                contactUri,
                arrayOf(
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER
                ),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                    if (idIndex != -1) {
                        contactId = cursor.getString(idIndex)
                    }
                    if (nameIndex != -1) {
                        name = cursor.getString(nameIndex) ?: ""
                    }

                    val hasPhone = if (hasPhoneIndex != -1) cursor.getInt(hasPhoneIndex) > 0 else false

                    if (hasPhone && contactId != null) {
                        contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                            arrayOf(contactId),
                            null
                        )?.use { phoneCursor ->
                            if (phoneCursor.moveToFirst()) {
                                val numberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                if (numberIndex != -1) {
                                    phone = phoneCursor.getString(numberIndex)?.replace(" ", "")?.replace("-", "")
                                }
                            }
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
