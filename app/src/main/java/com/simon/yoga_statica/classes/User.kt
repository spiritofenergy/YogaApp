package com.simon.yoga_statica.classes

import android.net.Uri

class User {
    lateinit var id: String
    lateinit var name: String
    var email: String? = null
    var phone: String? = null
    var countAsuns: Int = 0
    var status: Int = 1
    var photo: Uri? = null
    var promocode: String? = null
}