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
package com.vitorpamplona.amethyst.ui.note.types

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.get
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.SuccessResult
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.model.Note
import com.vitorpamplona.amethyst.model.User
import com.vitorpamplona.amethyst.ui.note.ClickableUserPicture
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.amethyst.ui.stringRes
import com.vitorpamplona.amethyst.ui.theme.Size35dp
import com.vitorpamplona.amethyst.ui.theme.ThemeComparisonRow
import com.vitorpamplona.amethyst.ui.theme.mediumImportanceLink
import com.vitorpamplona.quartz.events.BadgeAwardEvent
import com.vitorpamplona.quartz.events.BadgeDefinitionEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun BadgeDisplay(baseNote: Note) {
    val observingNote by baseNote.live().metadata.observeAsState()
    val badgeData = observingNote?.note?.event as? BadgeDefinitionEvent ?: return

    val image = badgeData.image()
    val name = badgeData.name()
    val description = badgeData.description()

    val background = MaterialTheme.colorScheme.background
    var backgroundFromImage by remember { mutableStateOf(Pair(background, background)) }
    var imageResult by remember { mutableStateOf<SuccessResult?>(null) }

    LaunchedEffect(key1 = imageResult) {
        launch(Dispatchers.IO) {
            imageResult?.let {
                val backgroundColor =
                    it.drawable
                        .toBitmap(200, 200)
                        .copy(Bitmap.Config.ARGB_8888, false)
                        .get(0, 199)
                val colorFromImage = Color(backgroundColor)
                val textBackground =
                    if (colorFromImage.luminance() > 0.5) {
                        lightColorScheme().onBackground
                    } else {
                        darkColorScheme().onBackground
                    }

                launch(Dispatchers.Main) { backgroundFromImage = Pair(colorFromImage, textBackground) }
            }
        }
    }

    RenderBadge(
        image,
        name,
        backgroundFromImage.first,
        backgroundFromImage.second,
        description,
    ) {
        if (imageResult == null) {
            imageResult = it.result
        }
    }
}

@Preview
@Composable
private fun RenderBadgePreview() {
    val background = MaterialTheme.colorScheme.background

    ThemeComparisonRow {
        RenderBadge(
            image = "http://test.com",
            name = "Name",
            backgroundForRow = background,
            backgroundFromImage = Color.LightGray,
            description = "This badge is awarded to the dedicated individuals who actively contributed by writing events to the relay during the crucial testing phase leading up to the first beta release of Grain.",
        ) {
        }
    }
}

@Composable
private fun RenderBadge(
    image: String?,
    name: String?,
    backgroundForRow: Color,
    backgroundFromImage: Color,
    description: String?,
    onSuccess: (AsyncImagePainter.State.Success) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .padding(10.dp)
                .clip(shape = CutCornerShape(20, 20, 20, 20))
                .aspectRatio(0.8f)
                .border(
                    5.dp,
                    MaterialTheme.colorScheme.mediumImportanceLink,
                    CutCornerShape(20),
                ).background(backgroundForRow),
    ) {
        Column {
            image?.let {
                AsyncImage(
                    model = it,
                    contentDescription =
                        stringRes(
                            R.string.badge_award_image_for,
                            name ?: "",
                        ),
                    modifier = Modifier.weight(1f),
                    contentScale = ContentScale.FillWidth,
                    onSuccess = onSuccess,
                )
            }

            name?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp, end = 10.dp),
                    color = backgroundFromImage,
                )
            }

            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
                    color = Color.Gray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RenderBadgeAward(
    note: Note,
    backgroundColor: MutableState<Color>,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
) {
    if (note.replyTo.isNullOrEmpty()) return

    val noteEvent = note.event as? BadgeAwardEvent ?: return
    var awardees by remember { mutableStateOf<List<User>>(listOf()) }

    Text(text = stringRes(R.string.award_granted_to))

    LaunchedEffect(key1 = note) { accountViewModel.loadUsers(noteEvent.awardees()) { awardees = it } }

    FlowRow(modifier = Modifier.padding(top = 5.dp)) {
        awardees.take(100).forEach { user ->
            Row(
                modifier =
                    Modifier
                        .size(size = Size35dp)
                        .clickable { nav("User/${user.pubkeyHex}") },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ClickableUserPicture(
                    baseUser = user,
                    accountViewModel = accountViewModel,
                    size = Size35dp,
                )
            }
        }

        if (awardees.size > 100) {
            Text(" and ${awardees.size - 100} others", maxLines = 1)
        }
    }

    note.replyTo?.firstOrNull()?.let {
        BadgeDisplay(baseNote = it)
    }
}
