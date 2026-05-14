package io.shellify.app.util

import io.shellify.app.core.security.Base64Codec

object JavaStandardBase64Codec : Base64Codec {
    override fun encode(bytes: ByteArray): String =
        java.util.Base64.getEncoder().encodeToString(bytes)

    override fun decode(str: String): ByteArray =
        java.util.Base64.getDecoder().decode(str)
}

object JavaUrlSafeBase64Codec : Base64Codec {
    override fun encode(bytes: ByteArray): String =
        java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    override fun decode(str: String): ByteArray =
        java.util.Base64.getUrlDecoder().decode(str)
}
