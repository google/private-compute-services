/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.personalcontext.ace.client.prototype.contact

import android.os.Bundle
import com.android.personalcontext.ace.client.prototype.PrototypeHint
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.ContactHintId
import java.time.LocalDate

/**
 * A [PrototypeHint] for Contact related use cases.
 *
 * @property name The Contact's display name. It will be only used for display purpose and not used
 *   in retrieval and filtering.
 * @property phoneNumbers List of Contact's phone numbers in E.164 format. Stored as String and will
 *   be converted to phone number objects in usage.
 * @property lookupKey Android Contacts lookup key. This is a unique identifier for a contact,
 *   generated when a contact is created. For more details, see:
 *   [android.provider.ContactsContract.ContactsColumns.LOOKUP_KEY]
 * @property emails List of Contact's email addresses.
 * @property addresses List of Contact's physical addresses.
 * @property birthday Contact's birthday, if any.
 */
data class ContactHint(
  val name: String,
  val phoneNumbers: List<String>,
  val lookupKey: String,
  val emails: List<String> = emptyList(),
  val addresses: List<String> = emptyList(),
  val birthday: LocalDate? = null,
  // ...Add other Contact information in the future
) : PrototypeHint(ContactHintId, this) {

  override fun exportDataToBundle(bundle: Bundle) {
    bundle.putString(KEY_NAME, name)
    bundle.putStringArrayList(KEY_PHONE_NUMBERS, ArrayList(phoneNumbers))
    bundle.putString(KEY_LOOKUP_KEY, lookupKey)
    bundle.putStringArrayList(KEY_EMAILS, ArrayList(emails))
    bundle.putString(KEY_BIRTHDAY, birthday?.toString())
  }

  companion object : Creator {
    private const val KEY_NAME = "name"
    private const val KEY_PHONE_NUMBERS = "phone_numbers"
    private const val KEY_LOOKUP_KEY = "lookup_key"
    private const val KEY_EMAILS = "emails"
    private const val KEY_ADDRESSES = "addresses"
    private const val KEY_BIRTHDAY = "birthday"

    override fun create(bundle: Bundle): PrototypeHint {
      val name = bundle.getString(KEY_NAME) ?: ""
      val phoneNumbers = bundle.getStringArrayList(KEY_PHONE_NUMBERS) ?: emptyList<String>()
      val lookupKey = bundle.getString(KEY_LOOKUP_KEY) ?: ""
      val emails = bundle.getStringArrayList(KEY_EMAILS) ?: emptyList<String>()
      val addresses = bundle.getStringArrayList(KEY_ADDRESSES) ?: emptyList<String>()
      val birthdayString = bundle.getString(KEY_BIRTHDAY)
      val birthday = birthdayString?.let { LocalDate.parse(it) }
      return ContactHint(name, phoneNumbers, lookupKey, emails, addresses, birthday)
    }
  }
}
