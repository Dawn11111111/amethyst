package com.vitorpamplona.amethyst.ui.dal

import com.vitorpamplona.amethyst.model.LocalCache
import com.vitorpamplona.amethyst.model.Note
import com.vitorpamplona.amethyst.model.User
import com.vitorpamplona.amethyst.service.model.AppRecommendationEvent

class UserProfileAppRecommendationsFeedFilter(val user: User) : FeedFilter<Note>() {
    override fun feedKey(): String {
        return user.pubkeyHex
    }

    override fun feed(): List<Note> {
        val recommendations = LocalCache.addressables.values.filter {
            (it.event as? AppRecommendationEvent)?.pubKey == user.pubkeyHex
        }.mapNotNull {
            (it.event as? AppRecommendationEvent)?.recommendations()
        }.flatten()
            .map {
                LocalCache.getOrCreateAddressableNote(it)
            }.toSet().toList()

        return recommendations
    }
}
