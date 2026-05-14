package io.shellify.app.core.security

import android.util.Base64

interface Base64Codec {
    fun encode(bytes: ByteArray): String
    fun decode(str: String): ByteArray
}

object StandardBase64Codec : Base64Codec {
    override fun encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
    override fun decode(str: String): ByteArray = Base64.decode(str, Base64.NO_WRAP)
}

object UrlSafeBase64Codec : Base64Codec {
    override fun encode(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

    override fun decode(str: String): ByteArray =
        Base64.decode(str, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
}
