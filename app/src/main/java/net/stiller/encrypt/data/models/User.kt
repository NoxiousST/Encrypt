package net.stiller.encrypt.data.models

data class User constructor (
    @JvmField var id: String? = null,
    @JvmField var username: String = "",
    @JvmField var email: String = "",
    @JvmField var password: String = "",
    @JvmField var picture: String? = null,
    @JvmField var phone: String = "",
    @JvmField var gender: String? = null,
    @JvmField var birthdate: String? = null
)