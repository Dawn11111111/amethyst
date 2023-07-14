package com.vitorpamplona.amethyst.service.model

import androidx.compose.runtime.Immutable
import com.vitorpamplona.amethyst.model.HexKey
import java.math.BigDecimal

@Immutable
interface EventInterface {
    fun id(): HexKey

    fun pubKey(): HexKey

    fun createdAt(): Long

    fun kind(): Int

    fun tags(): List<List<String>>

    fun content(): String

    fun sig(): HexKey

    fun toJson(): String

    fun checkSignature()

    fun hasValidSignature(): Boolean

    fun isTaggedUser(idHex: String): Boolean
    fun isTaggedAddressableNote(idHex: String): Boolean

    fun isTaggedHash(hashtag: String): Boolean
    fun isTaggedHashes(hashtags: Set<String>): Boolean
    fun firstIsTaggedHashes(hashtags: Set<String>): String?
    fun firstIsTaggedAddressableNote(addressableNotes: Set<String>): String?

    fun isTaggedAddressableKind(kind: Int): Boolean
    fun getTagOfAddressableKind(kind: Int): ATag?

    fun hashtags(): List<String>

    fun getReward(): BigDecimal?
    fun getPoWRank(): Int

    fun zapAddress(): String?
    fun isSensitive(): Boolean
    fun zapraiserAmount(): Long?

    fun taggedEmojis(): List<EmojiUrl>
    fun matchTag1With(text: String): Boolean
}
