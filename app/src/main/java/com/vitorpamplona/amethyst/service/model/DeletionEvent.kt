package com.vitorpamplona.amethyst.service.model

import androidx.compose.runtime.Immutable
import com.vitorpamplona.amethyst.model.HexKey
import com.vitorpamplona.amethyst.model.TimeUtils
import com.vitorpamplona.amethyst.model.toHexKey
import com.vitorpamplona.amethyst.service.CryptoUtils

@Immutable
class DeletionEvent(
    id: HexKey,
    pubKey: HexKey,
    createdAt: Long,
    tags: List<List<String>>,
    content: String,
    sig: HexKey
) : Event(id, pubKey, createdAt, kind, tags, content, sig) {
    fun deleteEvents() = tags.map { it[1] }

    companion object {
        const val kind = 5

        fun create(deleteEvents: List<String>, privateKey: ByteArray, createdAt: Long = TimeUtils.now()): DeletionEvent {
            val content = ""
            val pubKey = CryptoUtils.pubkeyCreate(privateKey).toHexKey()
            val tags = deleteEvents.map { listOf("e", it) }
            val id = generateId(pubKey, createdAt, kind, tags, content)
            val sig = CryptoUtils.sign(id, privateKey)
            return DeletionEvent(id.toHexKey(), pubKey, createdAt, tags, content, sig.toHexKey())
        }
    }
}
