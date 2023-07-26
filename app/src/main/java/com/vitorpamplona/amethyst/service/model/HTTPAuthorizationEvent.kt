package com.vitorpamplona.amethyst.service.model

import androidx.compose.runtime.Immutable
import com.vitorpamplona.amethyst.model.HexKey
import com.vitorpamplona.amethyst.model.TimeUtils
import com.vitorpamplona.amethyst.model.toHexKey
import com.vitorpamplona.amethyst.service.CryptoUtils

@Immutable
class HTTPAuthorizationEvent(
    id: HexKey,
    pubKey: HexKey,
    createdAt: Long,
    tags: List<List<String>>,
    content: String,
    sig: HexKey
) : Event(id, pubKey, createdAt, kind, tags, content, sig) {

    companion object {
        const val kind = 27235

        fun create(
            url: String,
            method: String,
            body: String? = null,
            privateKey: ByteArray,
            createdAt: Long = TimeUtils.now()
        ): HTTPAuthorizationEvent {
            var hash = ""
            body?.let {
                hash = CryptoUtils.sha256(it.toByteArray()).toHexKey()
            }

            val tags = listOfNotNull(
                listOf("u", url),
                listOf("method", method),
                listOf("payload", hash)
            )

            val pubKey = CryptoUtils.pubkeyCreate(privateKey).toHexKey()
            val id = generateId(pubKey, createdAt, kind, tags, "")
            val sig = CryptoUtils.sign(id, privateKey)
            return HTTPAuthorizationEvent(id.toHexKey(), pubKey, createdAt, tags, "", sig.toHexKey())
        }
    }
}
