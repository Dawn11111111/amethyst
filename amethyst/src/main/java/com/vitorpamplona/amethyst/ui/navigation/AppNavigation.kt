/**
 * Copyright (c) 2024 Vitor Pamplona
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.vitorpamplona.amethyst.ui.navigation

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.ui.MainActivity
import com.vitorpamplona.amethyst.ui.screen.SharedPreferencesViewModel
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.amethyst.ui.screen.loggedIn.BookmarkListScreen
import com.vitorpamplona.amethyst.ui.screen.loggedIn.DraftListScreen
import com.vitorpamplona.amethyst.ui.screen.loggedIn.GeoHashScreen
import com.vitorpamplona.amethyst.ui.screen.loggedIn.HashtagScreen
import com.vitorpamplona.amethyst.ui.screen.loggedIn.HiddenUsersScreen
import com.vitorpamplona.amethyst.ui.screen.loggedIn.LoadRedirectScreen
import com.vitorpamplona.amethyst.ui.screen.loggedIn.ProfileScreen
import com.vitorpamplona.amethyst.ui.screen.loggedIn.SearchScreen
import com.vitorpamplona.amethyst.ui.screen.loggedIn.SettingsScreen
import com.vitorpamplona.amethyst.ui.screen.loggedIn.ThreadScreen
import com.vitorpamplona.amethyst.ui.screen.loggedIn.chatrooms.ChannelScreen
import com.vitorpamplona.amethyst.ui.screen.loggedIn.chatrooms.ChatroomListScreen
import com.vitorpamplona.amethyst.ui.screen.loggedIn.chatrooms.ChatroomScreen
import com.vitorpamplona.amethyst.ui.screen.loggedIn.chatrooms.ChatroomScreenByAuthor
import com.vitorpamplona.amethyst.ui.screen.loggedIn.discover.CommunityScreen
import com.vitorpamplona.amethyst.ui.screen.loggedIn.discover.DiscoverScreen
import com.vitorpamplona.amethyst.ui.screen.loggedIn.discover.NIP90ContentDiscoveryScreen
import com.vitorpamplona.amethyst.ui.screen.loggedIn.home.HomeScreen
import com.vitorpamplona.amethyst.ui.screen.loggedIn.notifications.NotificationScreen
import com.vitorpamplona.amethyst.ui.screen.loggedIn.video.VideoScreen
import com.vitorpamplona.amethyst.ui.uriToRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLDecoder

@Composable
fun AppNavigation(
    navController: NavHostController,
    accountViewModel: AccountViewModel,
    sharedPreferencesViewModel: SharedPreferencesViewModel,
) {
    val scope = rememberCoroutineScope()
    val nav =
        remember(navController) {
            { route: String ->
                scope.launch {
                    if (getRouteWithArguments(navController) != route) {
                        navController.navigate(route)
                    }
                }
                Unit
            }
        }

    NavHost(
        navController,
        startDestination = Route.Home.route,
        enterTransition = { fadeIn(animationSpec = tween(200)) },
        exitTransition = { fadeOut(animationSpec = tween(200)) },
    ) {
        Route.Home.let { route ->
            composable(
                route.route,
                route.arguments,
                content = { it ->
                    val nip47 = it.arguments?.getString("nip47")

                    HomeScreen(
                        newThreadsFeedState = accountViewModel.feedStates.homeNewThreads,
                        repliesFeedState = accountViewModel.feedStates.homeReplies,
                        accountViewModel = accountViewModel,
                        nav = nav,
                        nip47 = nip47,
                    )

                    if (nip47 != null) {
                        LaunchedEffect(key1 = Unit) {
                            launch {
                                delay(1000)
                                it.arguments?.remove("nip47")
                            }
                        }
                    }
                },
            )
        }

        composable(
            Route.Message.route,
            content = {
                ChatroomListScreen(
                    accountViewModel.feedStates.dmKnown,
                    accountViewModel.feedStates.dmNew,
                    accountViewModel,
                    nav,
                )
            },
        )

        Route.Video.let { route ->
            composable(
                route.route,
                route.arguments,
                content = {
                    VideoScreen(
                        videoFeedContentState = accountViewModel.feedStates.videoFeed,
                        accountViewModel = accountViewModel,
                        nav = nav,
                    )
                },
            )
        }

        Route.Discover.let { route ->
            composable(
                route.route,
                route.arguments,
                content = {
                    DiscoverScreen(
                        discoveryContentNIP89FeedContentState = accountViewModel.feedStates.discoverDVMs,
                        discoveryMarketplaceFeedContentState = accountViewModel.feedStates.discoverMarketplace,
                        discoveryLiveFeedContentState = accountViewModel.feedStates.discoverLive,
                        discoveryCommunityFeedContentState = accountViewModel.feedStates.discoverCommunities,
                        discoveryChatFeedContentState = accountViewModel.feedStates.discoverPublicChats,
                        accountViewModel = accountViewModel,
                        nav = nav,
                    )
                },
            )
        }

        Route.Search.let { route ->
            composable(
                route.route,
                route.arguments,
                content = {
                    SearchScreen(
                        accountViewModel = accountViewModel,
                        nav = nav,
                    )
                },
            )
        }

        Route.Notification.let { route ->
            composable(
                route.route,
                route.arguments,
                content = {
                    NotificationScreen(
                        notifFeedContentState = accountViewModel.feedStates.notifications,
                        notifSummaryState = accountViewModel.feedStates.notificationSummary,
                        sharedPreferencesViewModel = sharedPreferencesViewModel,
                        accountViewModel = accountViewModel,
                        nav = nav,
                    )
                },
            )
        }

        composable(Route.BlockedUsers.route, content = { HiddenUsersScreen(accountViewModel, nav) })
        composable(Route.Bookmarks.route, content = { BookmarkListScreen(accountViewModel, nav) })
        composable(Route.Drafts.route, content = { DraftListScreen(accountViewModel, nav) })

        Route.ContentDiscovery.let { route ->
            composable(
                route.route,
                route.arguments,
                content = {
                    it.arguments?.getString("id")?.let { id ->
                        NIP90ContentDiscoveryScreen(
                            appDefinitionEventId = id,
                            accountViewModel = accountViewModel,
                            nav = nav,
                        )
                    }
                },
            )
        }

        Route.Profile.let { route ->
            composable(
                route.route,
                route.arguments,
                content = {
                    ProfileScreen(
                        userId = it.arguments?.getString("id"),
                        accountViewModel = accountViewModel,
                        nav = nav,
                    )
                },
            )
        }

        Route.Note.let { route ->
            composable(
                route.route,
                route.arguments,
                content = {
                    ThreadScreen(
                        noteId = it.arguments?.getString("id"),
                        accountViewModel = accountViewModel,
                        nav = nav,
                    )
                },
            )
        }

        Route.Hashtag.let { route ->
            composable(
                route.route,
                route.arguments,
                content = {
                    HashtagScreen(
                        tag = it.arguments?.getString("id"),
                        accountViewModel = accountViewModel,
                        nav = nav,
                    )
                },
            )
        }

        Route.Geohash.let { route ->
            composable(
                route.route,
                route.arguments,
                content = {
                    GeoHashScreen(
                        tag = it.arguments?.getString("id"),
                        accountViewModel = accountViewModel,
                        nav = nav,
                    )
                },
            )
        }

        Route.Community.let { route ->
            composable(
                route.route,
                route.arguments,
                content = {
                    CommunityScreen(
                        aTagHex = it.arguments?.getString("id"),
                        accountViewModel = accountViewModel,
                        nav = nav,
                    )
                },
            )
        }

        Route.Room.let { route ->
            composable(
                route.route,
                route.arguments,
                content = {
                    val decodedMessage =
                        it.arguments?.getString("message")?.let {
                            URLDecoder.decode(it, "utf-8")
                        }
                    ChatroomScreen(
                        roomId = it.arguments?.getString("id"),
                        draftMessage = decodedMessage,
                        accountViewModel = accountViewModel,
                        nav = nav,
                    )
                },
            )
        }

        Route.RoomByAuthor.let { route ->
            composable(
                route.route,
                route.arguments,
                content = {
                    ChatroomScreenByAuthor(
                        authorPubKeyHex = it.arguments?.getString("id"),
                        accountViewModel = accountViewModel,
                        nav = nav,
                    )
                },
            )
        }

        Route.Channel.let { route ->
            composable(
                route.route,
                route.arguments,
                content = {
                    ChannelScreen(
                        channelId = it.arguments?.getString("id"),
                        accountViewModel = accountViewModel,
                        nav = nav,
                    )
                },
            )
        }

        Route.Event.let { route ->
            composable(
                route.route,
                route.arguments,
                content = {
                    LoadRedirectScreen(
                        eventId = it.arguments?.getString("id"),
                        accountViewModel = accountViewModel,
                        navController = navController,
                    )
                },
            )
        }

        Route.Settings.let { route ->
            composable(
                route.route,
                route.arguments,
                content = {
                    SettingsScreen(
                        sharedPreferencesViewModel,
                    )
                },
            )
        }
    }

    val activity = LocalContext.current.getActivity()

    var currentIntentNextPage by remember {
        mutableStateOf(
            activity.intent
                ?.data
                ?.toString()
                ?.ifBlank { null },
        )
    }

    currentIntentNextPage?.let { intentNextPage ->
        var actionableNextPage by remember {
            mutableStateOf(uriToRoute(intentNextPage))
        }

        LaunchedEffect(intentNextPage) {
            if (actionableNextPage != null) {
                actionableNextPage?.let {
                    val currentRoute = getRouteWithArguments(navController)
                    if (!isSameRoute(currentRoute, it)) {
                        navController.navigate(it) {
                            popUpTo(Route.Home.route)
                            launchSingleTop = true
                        }
                    }
                    actionableNextPage = null
                }
            } else {
                accountViewModel.toast(
                    R.string.invalid_nip19_uri,
                    R.string.invalid_nip19_uri_description,
                    intentNextPage,
                )
            }

            currentIntentNextPage = null
        }
    }

    DisposableEffect(activity) {
        val consumer =
            Consumer<Intent> { intent ->
                val uri = intent?.data?.toString()
                if (!uri.isNullOrBlank()) {
                    val newPage = uriToRoute(uri)

                    if (newPage != null) {
                        val currentRoute = getRouteWithArguments(navController)
                        if (!isSameRoute(currentRoute, newPage)) {
                            navController.navigate(newPage) {
                                popUpTo(Route.Home.route)
                                launchSingleTop = true
                            }
                        }
                    } else {
                        scope.launch {
                            delay(1000)
                            accountViewModel.toast(
                                R.string.invalid_nip19_uri,
                                R.string.invalid_nip19_uri_description,
                                uri,
                            )
                        }
                    }
                }
            }
        activity.addOnNewIntentListener(consumer)
        onDispose { activity.removeOnNewIntentListener(consumer) }
    }
}

fun Context.getActivity(): MainActivity {
    if (this is MainActivity) return this
    return if (this is ContextWrapper) baseContext.getActivity() else getActivity()
}

private fun isSameRoute(
    currentRoute: String?,
    newRoute: String,
): Boolean {
    if (currentRoute == null) return false

    if (currentRoute == newRoute) {
        return true
    }

    if (newRoute.startsWith("Event/") && currentRoute.contains("/")) {
        if (newRoute.split("/")[1] == currentRoute.split("/")[1]) {
            return true
        }
    }

    return false
}
