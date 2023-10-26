package com.vitorpamplona.amethyst.ui.screen.loggedIn

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.model.ConnectivityType
import com.vitorpamplona.amethyst.model.ThemeType
import com.vitorpamplona.amethyst.model.parseBooleanType
import com.vitorpamplona.amethyst.model.parseConnectivityType
import com.vitorpamplona.amethyst.model.parseThemeType
import com.vitorpamplona.amethyst.ui.components.PushNotificationSettingsRow
import com.vitorpamplona.amethyst.ui.screen.SharedPreferencesViewModel
import com.vitorpamplona.amethyst.ui.theme.DoubleVertSpacer
import com.vitorpamplona.amethyst.ui.theme.HalfVertSpacer
import com.vitorpamplona.amethyst.ui.theme.Size10dp
import com.vitorpamplona.amethyst.ui.theme.Size20dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

fun Context.getLocaleListFromXml(): LocaleListCompat {
    val tagsList = mutableListOf<CharSequence>()
    try {
        val xpp: XmlPullParser = resources.getXml(R.xml.locales_config)
        while (xpp.eventType != XmlPullParser.END_DOCUMENT) {
            if (xpp.eventType == XmlPullParser.START_TAG) {
                if (xpp.name == "locale") {
                    tagsList.add(xpp.getAttributeValue(0))
                }
            }
            xpp.next()
        }
    } catch (e: XmlPullParserException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return LocaleListCompat.forLanguageTags(tagsList.joinToString(","))
}

fun Context.getLangPreferenceDropdownEntries(): ImmutableMap<String, String> {
    val localeList = getLocaleListFromXml()
    val map = mutableMapOf<String, String>()

    for (a in 0 until localeList.size()) {
        localeList[a].let {
            map.put(it!!.getDisplayName(it).replaceFirstChar { char -> char.uppercase() }, it.toLanguageTag())
        }
    }
    return map.toImmutableMap()
}

fun getLanguageIndex(languageEntries: ImmutableMap<String, String>, sharedPreferencesViewModel: SharedPreferencesViewModel): Int {
    val language = sharedPreferencesViewModel.sharedPrefs.language
    var languageIndex = -1
    if (language != null) {
        languageIndex = languageEntries.values.toTypedArray().indexOf(language)
    } else {
        languageIndex = languageEntries.values.toTypedArray().indexOf(Locale.current.toLanguageTag())
    }
    if (languageIndex == -1) languageIndex = languageEntries.values.toTypedArray().indexOf(Locale.current.language)
    if (languageIndex == -1) languageIndex = languageEntries.values.toTypedArray().indexOf("en")
    return languageIndex
}

@Composable
fun SettingsScreen(
    sharedPreferencesViewModel: SharedPreferencesViewModel
) {
    val selectedItens = persistentListOf(
        TitleExplainer(stringResource(ConnectivityType.ALWAYS.resourceId)),
        TitleExplainer(stringResource(ConnectivityType.WIFI_ONLY.resourceId)),
        TitleExplainer(stringResource(ConnectivityType.NEVER.resourceId))
    )

    val themeItens = persistentListOf(
        TitleExplainer(stringResource(ThemeType.SYSTEM.resourceId)),
        TitleExplainer(stringResource(ThemeType.LIGHT.resourceId)),
        TitleExplainer(stringResource(ThemeType.DARK.resourceId))
    )

    val booleanItems = persistentListOf(
        TitleExplainer(stringResource(ConnectivityType.ALWAYS.resourceId)),
        TitleExplainer(stringResource(ConnectivityType.NEVER.resourceId))
    )

    val showImagesIndex = sharedPreferencesViewModel.sharedPrefs.automaticallyShowImages.screenCode
    val videoIndex = sharedPreferencesViewModel.sharedPrefs.automaticallyStartPlayback.screenCode
    val linkIndex = sharedPreferencesViewModel.sharedPrefs.automaticallyShowUrlPreview.screenCode
    val hideNavBarsIndex = sharedPreferencesViewModel.sharedPrefs.automaticallyHideNavigationBars.screenCode
    val profilePictureIndex = sharedPreferencesViewModel.sharedPrefs.automaticallyShowProfilePictures.screenCode
    val themeIndex = sharedPreferencesViewModel.sharedPrefs.theme.screenCode

    val context = LocalContext.current

    val languageEntries = remember {
        context.getLangPreferenceDropdownEntries()
    }
    val languageList = remember {
        languageEntries.keys.map { TitleExplainer(it) }.toImmutableList()
    }
    val languageIndex = getLanguageIndex(languageEntries, sharedPreferencesViewModel)

    Column(
        Modifier
            .padding(top = Size10dp, start = Size20dp, end = Size20dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SettingsRow(
            R.string.language,
            R.string.language_description,
            languageList,
            languageIndex
        ) {
            sharedPreferencesViewModel.updateLanguage(languageEntries[languageList[it].title])
        }

        Spacer(modifier = HalfVertSpacer)

        SettingsRow(
            R.string.theme,
            R.string.theme_description,
            themeItens,
            themeIndex
        ) {
            sharedPreferencesViewModel.updateTheme(parseThemeType(it))
        }

        Spacer(modifier = HalfVertSpacer)

        SettingsRow(
            R.string.automatically_load_images_gifs,
            R.string.automatically_load_images_gifs_description,
            selectedItens,
            showImagesIndex
        ) {
            sharedPreferencesViewModel.updateAutomaticallyShowImages(parseConnectivityType(it))
        }

        Spacer(modifier = HalfVertSpacer)

        SettingsRow(
            R.string.automatically_play_videos,
            R.string.automatically_play_videos_description,
            selectedItens,
            videoIndex
        ) {
            sharedPreferencesViewModel.updateAutomaticallyStartPlayback(parseConnectivityType(it))
        }

        Spacer(modifier = HalfVertSpacer)

        SettingsRow(
            R.string.automatically_show_url_preview,
            R.string.automatically_show_url_preview_description,
            selectedItens,
            linkIndex
        ) {
            sharedPreferencesViewModel.updateAutomaticallyShowUrlPreview(parseConnectivityType(it))
        }

        SettingsRow(
            R.string.automatically_show_profile_picture,
            R.string.automatically_show_profile_picture_description,
            selectedItens,
            profilePictureIndex
        ) {
            sharedPreferencesViewModel.updateAutomaticallyShowProfilePicture(parseConnectivityType(it))
        }

        Spacer(modifier = HalfVertSpacer)

        SettingsRow(
            R.string.automatically_hide_nav_bars,
            R.string.automatically_hide_nav_bars_description,
            booleanItems,
            hideNavBarsIndex
        ) {
            sharedPreferencesViewModel.updateAutomaticallyHideNavBars(parseBooleanType(it))
        }

        Spacer(modifier = HalfVertSpacer)

        PushNotificationSettingsRow(sharedPreferencesViewModel)
    }
}

@Composable
fun SettingsRow(
    name: Int,
    description: Int,
    selectedItens: ImmutableList<TitleExplainer>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.weight(2.0f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = stringResource(name),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(description),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        TextSpinner(
            label = "",
            placeholder = selectedItens[selectedIndex].title,
            options = selectedItens,
            onSelect = onSelect,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets(0.dp, 0.dp, 0.dp, 0.dp))
                .weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownSettings(
    selectedItem: MutableState<String>,
    listItems: Array<String>,
    title: String
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    ExposedDropdownMenuBox(
        modifier = Modifier.padding(8.dp),
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = selectedItem.value,
            onValueChange = {},
            readOnly = true,
            label = { Text(text = title) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listItems.forEach { selectedOption ->
                DropdownMenuItem(
                    text = {
                        Text(text = selectedOption)
                    },
                    onClick = {
                        selectedItem.value = selectedOption
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun Section(text: String) {
    Spacer(modifier = DoubleVertSpacer)
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )
    Spacer(modifier = DoubleVertSpacer)
}
