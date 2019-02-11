package tech.vault.oauth.android

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal sealed class VaultPayload {

    companion object {

        private const val HEX_CHARS = "0123456789abcdef"

        class Builder {
            fun dict(vararg payloads: Pair<String, VaultPayload>): Dict = Dict(payloads.toList())
            fun value(value: Any) = Value(value)
            fun list(vararg values: VaultPayload) = PayloadList(values.toList())
        }

        fun build(builderConfig: Builder.() -> VaultPayload): VaultPayload =
                Builder().let(builderConfig)

    }

    fun toSignature(key: String): String = hmacSHA512(key.toHexByteArray(), toString()).toHexString()

    class Dict(private val map: List<Pair<String, VaultPayload>>) : VaultPayload() {
        override fun toString(): String =
                map.sortedBy { it.first }
                        .joinToString("&") { (key, value) -> "$key=$value" }
    }

    class Value(private val value: Any) : VaultPayload() {
        override fun toString() = "$value"
    }

    class PayloadList(private val list: List<VaultPayload>) : VaultPayload() {
        override fun toString() = "[${list.joinToString(",")}]"
    }

    private fun hmacSHA512(key: ByteArray, data: String): ByteArray = try {
        val algorithm = "HmacSHA512"
        Mac.getInstance(algorithm).run {
            init(SecretKeySpec(key, algorithm))
            doFinal(data.toByteArray(charset("UTF8")))
        }
    } catch (e: Exception) {
        throw RuntimeException("Could not run HMAC SHA512", e)
    }

    private fun String.toHexByteArray(): ByteArray {
        val result = ByteArray(length / 2)
        for (i in 0 until length step 2) {
            val firstIndex = HEX_CHARS.indexOf(this[i])
            val secondIndex = HEX_CHARS.indexOf(this[i + 1])

            val octet = firstIndex.shl(4).or(secondIndex)
            result[i.shr(1)] = octet.toByte()
        }
        return result
    }

    private fun ByteArray.toHexString(): String =
            fold(StringBuilder()) { acc, next -> acc.append(String.format("%02x", next)) }
                    .toString()
                    .toLowerCase()
}


