package com.vitorpamplona.amethyst.ui.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vitorpamplona.amethyst.ui.note.BadgeCompose
import com.vitorpamplona.amethyst.ui.note.BoostSetCompose
import com.vitorpamplona.amethyst.ui.note.LikeSetCompose
import com.vitorpamplona.amethyst.ui.note.MessageSetCompose
import com.vitorpamplona.amethyst.ui.note.MultiSetCompose
import com.vitorpamplona.amethyst.ui.note.NoteCompose
import com.vitorpamplona.amethyst.ui.note.ZapSetCompose
import com.vitorpamplona.amethyst.ui.note.ZapUserSetCompose
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RefresheableCardView(
    viewModel: CardFeedViewModel,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
    routeForLastRead: String,
    scrollStateKey: String? = null,
    enablePullRefresh: Boolean = true
) {
    var refreshing by remember { mutableStateOf(false) }
    val refresh = { refreshing = true; viewModel.invalidateData(); refreshing = false }
    val pullRefreshState = rememberPullRefreshState(refreshing, onRefresh = refresh)

    val modifier = if (enablePullRefresh) {
        Modifier.pullRefresh(pullRefreshState)
    } else {
        Modifier
    }

    Box(modifier) {
        Column {
            SaveableCardFeedState(viewModel, accountViewModel, nav, routeForLastRead, scrollStateKey)
        }

        if (enablePullRefresh) {
            PullRefreshIndicator(refreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
private fun SaveableCardFeedState(
    viewModel: CardFeedViewModel,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
    routeForLastRead: String,
    scrollStateKey: String? = null
) {
    val listState = if (scrollStateKey != null) {
        rememberForeverLazyListState(scrollStateKey)
    } else {
        rememberLazyListState()
    }

    WatchScrollToTop(viewModel, listState)

    RenderCardFeed(viewModel, accountViewModel, listState, nav, routeForLastRead)
}

@Composable
private fun WatchScrollToTop(
    viewModel: CardFeedViewModel,
    listState: LazyListState
) {
    val scrollToTop by viewModel.scrollToTop.collectAsState()

    LaunchedEffect(scrollToTop) {
        if (scrollToTop > 0 && viewModel.scrolltoTopPending) {
            listState.scrollToItem(index = 0)
            viewModel.sentToTop()
        }
    }
}

@Composable
fun RenderCardFeed(
    viewModel: CardFeedViewModel,
    accountViewModel: AccountViewModel,
    listState: LazyListState,
    nav: (String) -> Unit,
    routeForLastRead: String
) {
    val feedState by viewModel.feedContent.collectAsState()

    Crossfade(
        modifier = Modifier.fillMaxSize(),
        targetState = feedState,
        animationSpec = tween(durationMillis = 100)
    ) { state ->

        when (state) {
            is CardFeedState.Empty -> {
                FeedEmpty {
                    viewModel.invalidateData()
                }
            }
            is CardFeedState.FeedError -> {
                FeedError(state.errorMessage) {
                    viewModel.invalidateData()
                }
            }
            is CardFeedState.Loaded -> {
                FeedLoaded(
                    state = state,
                    listState = listState,
                    accountViewModel = accountViewModel,
                    nav = nav,
                    routeForLastRead = routeForLastRead
                )
            }
            CardFeedState.Loading -> {
                LoadingFeed()
            }
        }
    }
}

@Composable
private fun FeedLoaded(
    state: CardFeedState.Loaded,
    listState: LazyListState,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
    routeForLastRead: String
) {
    val defaultModifier = remember {
        Modifier.fillMaxWidth().defaultMinSize(minHeight = 100.dp)
    }

    LazyColumn(
        modifier = remember { Modifier.fillMaxSize() },
        contentPadding = remember {
            PaddingValues(
                top = 10.dp,
                bottom = 10.dp
            )
        },
        state = listState
    ) {
        itemsIndexed(state.feed.value, key = { _, item -> item.id() }) { _, item ->
            Row(defaultModifier) {
                when (item) {
                    is NoteCard -> NoteCompose(
                        item.note,
                        isBoostedNote = false,
                        accountViewModel = accountViewModel,
                        nav = nav,
                        routeForLastRead = routeForLastRead
                    )

                    is ZapSetCard -> ZapSetCompose(
                        item,
                        isInnerNote = false,
                        accountViewModel = accountViewModel,
                        nav = nav,
                        routeForLastRead = routeForLastRead
                    )

                    is ZapUserSetCard -> ZapUserSetCompose(
                        item,
                        isInnerNote = false,
                        accountViewModel = accountViewModel,
                        nav = nav,
                        routeForLastRead = routeForLastRead
                    )

                    is LikeSetCard -> LikeSetCompose(
                        item,
                        isInnerNote = false,
                        accountViewModel = accountViewModel,
                        nav = nav,
                        routeForLastRead = routeForLastRead
                    )

                    is BoostSetCard -> BoostSetCompose(
                        item,
                        isInnerNote = false,
                        accountViewModel = accountViewModel,
                        nav = nav,
                        routeForLastRead = routeForLastRead
                    )

                    is MultiSetCard -> MultiSetCompose(
                        item,
                        accountViewModel = accountViewModel,
                        nav = nav,
                        routeForLastRead = routeForLastRead
                    )

                    is BadgeCard -> BadgeCompose(
                        item,
                        accountViewModel = accountViewModel,
                        nav = nav,
                        routeForLastRead = routeForLastRead
                    )

                    is MessageSetCard -> MessageSetCompose(
                        messageSetCard = item,
                        routeForLastRead = routeForLastRead,
                        accountViewModel = accountViewModel,
                        nav = nav
                    )
                }
            }
        }
    }
}
