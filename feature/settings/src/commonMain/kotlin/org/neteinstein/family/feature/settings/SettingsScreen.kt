package org.neteinstein.family.feature.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.neteinstein.family.domain.model.AppLanguage
import org.neteinstein.family.domain.model.ThemeMode
import org.neteinstein.family.feature.settings.platform.rememberCurrentVersionName
import org.neteinstein.family.feature.settings.platform.rememberOpenLanguageSettingsAction
import org.neteinstein.family.feature.settings.resources.Res
import org.neteinstein.family.feature.settings.resources.app_name
import org.neteinstein.family.feature.settings.resources.cancel_button
import org.neteinstein.family.feature.settings.resources.cd_back
import org.neteinstein.family.feature.settings.resources.checking_updates
import org.neteinstein.family.feature.settings.resources.downloading_update
import org.neteinstein.family.feature.settings.resources.enable_installing_updates
import org.neteinstein.family.feature.settings.resources.loopgain_name
import org.neteinstein.family.feature.settings.resources.pedro_vicente_name
import org.neteinstein.family.feature.settings.resources.reset_button
import org.neteinstein.family.feature.settings.resources.reset_cards_button
import org.neteinstein.family.feature.settings.resources.reset_cards_description
import org.neteinstein.family.feature.settings.resources.reset_cards_done
import org.neteinstein.family.feature.settings.resources.reset_cards_hidden_count_format
import org.neteinstein.family.feature.settings.resources.reset_dialog_message
import org.neteinstein.family.feature.settings.resources.reset_dialog_title
import org.neteinstein.family.feature.settings.resources.resetting_cards
import org.neteinstein.family.feature.settings.resources.section_about
import org.neteinstein.family.feature.settings.resources.section_app_preferences
import org.neteinstein.family.feature.settings.resources.section_reset_cards
import org.neteinstein.family.feature.settings.resources.section_updates
import org.neteinstein.family.feature.settings.resources.settings_about_description
import org.neteinstein.family.feature.settings.resources.settings_app_title_format
import org.neteinstein.family.feature.settings.resources.settings_language_automatic
import org.neteinstein.family.feature.settings.resources.settings_language_subtitle
import org.neteinstein.family.feature.settings.resources.settings_language_title
import org.neteinstein.family.feature.settings.resources.settings_loopgain_footer
import org.neteinstein.family.feature.settings.resources.settings_theme_dark
import org.neteinstein.family.feature.settings.resources.settings_theme_light
import org.neteinstein.family.feature.settings.resources.settings_theme_subtitle
import org.neteinstein.family.feature.settings.resources.settings_theme_system
import org.neteinstein.family.feature.settings.resources.settings_theme_title
import org.neteinstein.family.feature.settings.resources.settings_title
import org.neteinstein.family.feature.settings.resources.settings_version_format
import org.neteinstein.family.feature.settings.resources.sideloading_blocked_message
import org.neteinstein.family.feature.settings.resources.up_to_date_format
import org.neteinstein.family.feature.settings.resources.update_button
import org.neteinstein.family.feature.settings.resources.update_description

// The naked/apex domain (loopgain.org) doesn't resolve - only the www subdomain does.
private const val LOOPGAIN_URL = "http://www.loopgain.org"
private const val PEDRO_VICENTE_URL = "https://www.pedrovicente.pt"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    val openLanguageSettings = rememberOpenLanguageSettingsAction()

    val linkStyles =
        TextLinkStyles(
            style =
                SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                ),
        )
    val appName = stringResource(Res.string.app_name)
    val loopGain = stringResource(Res.string.loopgain_name)
    val appTitle = stringResource(Res.string.settings_app_title_format, appName, loopGain)
    val annotatedAppTitle =
        remember(appTitle, loopGain, linkStyles) {
            buildAnnotatedString {
                append(appTitle)
                addUrlLink(appTitle, loopGain, LOOPGAIN_URL, linkStyles)
            }
        }
    val versionName = rememberCurrentVersionName()

    LaunchedEffect(Unit) {
        viewModel.onScreenEntered()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.settings_title),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.cd_back),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            )
        },
    ) { innerPadding ->
        Surface(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
            ) {
                Text(
                    text = stringResource(Res.string.section_app_preferences),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ThemeModeSelector(
                        selected = uiState.themeMode,
                        onThemeModeSelected = viewModel::onThemeModeSelected,
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Android deep-links into the OS's own per-app language settings. iOS/Web have
                    // no such settings page (see rememberOpenLanguageSettingsAction's doc comment),
                    // so they get an in-app picker instead of a dead "change language" row.
                    if (openLanguageSettings != null) {
                        SettingsItem(
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            title = stringResource(Res.string.settings_language_title),
                            subtitle = stringResource(Res.string.settings_language_subtitle),
                            onClick = openLanguageSettings,
                        )
                    } else {
                        LanguagePicker(
                            selected = uiState.languageOverride,
                            onLanguageSelected = viewModel::onLanguageSelected,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(Res.string.section_reset_cards),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ResetCardsSection(
                        status = uiState.resetCardsStatus,
                        hiddenCardsCount = uiState.hiddenCardsCount,
                        totalCardsCount = uiState.totalCardsCount,
                        onResetCardsClicked = { showResetConfirmDialog = true },
                    )
                }

                if (uiState.updatesEnabled) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(Res.string.section_updates),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        UpdateSection(
                            status = uiState.updateStatus,
                            onUpdateClicked = viewModel::onUpdateClicked,
                            onEnableSideloadingClicked = viewModel::onEnableSideloadingClicked,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(Res.string.section_about),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = annotatedAppTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(Res.string.settings_version_format, versionName),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(Res.string.settings_about_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                LoopGainFooter(modifier = Modifier.fillMaxWidth())
            }
        }
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text(stringResource(Res.string.reset_dialog_title)) },
            text = { Text(stringResource(Res.string.reset_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetConfirmDialog = false
                        viewModel.onResetCardsClicked()
                    },
                ) {
                    Text(stringResource(Res.string.reset_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text(stringResource(Res.string.cancel_button))
                }
            },
        )
    }
}

/**
 * Info text + button that makes every card hidden via swipe-down on Home visible again (see
 * [SettingsViewModel.onResetCardsClicked]). The actual reset only runs after the caller confirms
 * in a dialog, since it's a bulk, hard-to-undo action.
 */
@Composable
private fun ResetCardsSection(
    status: ResetCardsStatus,
    hiddenCardsCount: Int,
    totalCardsCount: Int,
    onResetCardsClicked: () -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text =
                stringResource(Res.string.reset_cards_description) + " " +
                    stringResource(Res.string.reset_cards_hidden_count_format, hiddenCardsCount, totalCardsCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onResetCardsClicked,
            enabled = status != ResetCardsStatus.Resetting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(Res.string.reset_cards_button))
        }

        when (status) {
            is ResetCardsStatus.Idle -> Unit
            is ResetCardsStatus.Resetting -> UpdateStatusRow(text = stringResource(Res.string.resetting_cards))
            is ResetCardsStatus.Done ->
                Text(
                    text = stringResource(Res.string.reset_cards_done),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
        }
    }
}

/**
 * Info text + button that checks GitHub Releases and, if allowed, downloads and installs a newer
 * build (see [SettingsViewModel.onUpdateClicked]). The button stays enabled in every state except
 * while a check/download is actually in flight, so [UpdateStatus.SideloadingBlocked]/
 * [UpdateStatus.Failed]/[UpdateStatus.UpToDate] can all be retried with a plain second tap - e.g.
 * after the user enables sideloading in system Settings and returns to this screen.
 */
@Composable
private fun UpdateSection(
    status: UpdateStatus,
    onUpdateClicked: () -> Unit,
    onEnableSideloadingClicked: () -> Unit,
) {
    val isBusy = status is UpdateStatus.Checking || status is UpdateStatus.Downloading

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(Res.string.update_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onUpdateClicked,
            enabled = !isBusy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(Res.string.update_button))
        }

        when (status) {
            is UpdateStatus.Idle -> Unit
            is UpdateStatus.Checking -> UpdateStatusRow(text = stringResource(Res.string.checking_updates))
            is UpdateStatus.Downloading -> UpdateStatusRow(text = stringResource(Res.string.downloading_update))
            is UpdateStatus.UpToDate ->
                Text(
                    text = stringResource(Res.string.up_to_date_format, status.currentVersionName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            is UpdateStatus.UpdateAvailable -> Unit
            is UpdateStatus.Failed ->
                Text(
                    text = status.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
            is UpdateStatus.SideloadingBlocked -> {
                Text(
                    text = stringResource(Res.string.sideloading_blocked_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onEnableSideloadingClicked,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(Res.string.enable_installing_updates))
                }
            }
        }
    }
}

@Composable
private fun UpdateStatusRow(text: String) {
    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

/**
 * Attribution line closing the screen: the app's place in the wider LoopGain tool family, with the
 * brand and the author each linking out to their own site.
 */
@Composable
private fun LoopGainFooter(modifier: Modifier = Modifier) {
    val loopGain = stringResource(Res.string.loopgain_name)
    val author = stringResource(Res.string.pedro_vicente_name)
    val footer = stringResource(Res.string.settings_loopgain_footer, loopGain, author)
    val linkStyles =
        TextLinkStyles(
            style =
                SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                ),
        )

    val annotatedFooter =
        remember(footer, loopGain, author, linkStyles) {
            buildAnnotatedString {
                append(footer)
                addUrlLink(footer, loopGain, LOOPGAIN_URL, linkStyles)
                addUrlLink(footer, author, PEDRO_VICENTE_URL, linkStyles)
            }
        }

    Text(
        text = annotatedFooter,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

/**
 * Links the first occurrence of [label] in [text] to [url]. A translation that drops or rewrites
 * the placeholder simply keeps that part as plain text instead of blowing up on a bad range.
 */
private fun AnnotatedString.Builder.addUrlLink(
    text: String,
    label: String,
    url: String,
    styles: TextLinkStyles,
) {
    val start = text.indexOf(label)
    if (start < 0) return
    addLink(LinkAnnotation.Url(url, styles), start, start + label.length)
}

/** Lets the user force the app's appearance to light/dark, or follow the OS setting. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeModeSelector(
    selected: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(Res.string.settings_theme_title),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(Res.string.settings_theme_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(12.dp))

        val options = ThemeMode.entries
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, themeMode ->
                val isSelected = themeMode == selected
                SegmentedButton(
                    selected = isSelected,
                    onClick = { onThemeModeSelected(themeMode) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    icon = {
                        SegmentedButtonDefaults.Icon(active = isSelected) {
                            Icon(
                                imageVector = themeMode.icon(),
                                contentDescription = null,
                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize),
                            )
                        }
                    },
                    label = { Text(text = stringResource(themeMode.labelRes())) },
                )
            }
        }
    }
}

private fun ThemeMode.icon() =
    when (this) {
        ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
        ThemeMode.LIGHT -> Icons.Default.LightMode
        ThemeMode.DARK -> Icons.Default.DarkMode
    }

private fun ThemeMode.labelRes() =
    when (this) {
        ThemeMode.SYSTEM -> Res.string.settings_theme_system
        ThemeMode.LIGHT -> Res.string.settings_theme_light
        ThemeMode.DARK -> Res.string.settings_theme_dark
    }

@Composable
private fun SettingsItem(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(0.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon()
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * In-app replacement for Android's "open system language settings" row, used on iOS/Web where no
 * such settings page exists (see [rememberOpenLanguageSettingsAction]'s doc comment). [selected]
 * is the current override ([SettingsUiState.languageOverride]) - `null` shows
 * [Res.string.settings_language_automatic] and means "follow the OS/browser language" (see
 * [org.neteinstein.family.domain.usecase.GetContentLanguageUseCase]).
 */
@Composable
private fun LanguagePicker(
    selected: AppLanguage?,
    onLanguageSelected: (AppLanguage?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val automaticLabel = stringResource(Res.string.settings_language_automatic)
    val selectedLabel = selected?.nativeName ?: automaticLabel

    Box {
        SettingsItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            },
            title = stringResource(Res.string.settings_language_title),
            subtitle = selectedLabel,
            onClick = { expanded = true },
        )
        val checkIcon: @Composable () -> Unit = { Icon(imageVector = Icons.Default.Check, contentDescription = null) }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(automaticLabel) },
                trailingIcon = if (selected == null) checkIcon else null,
                onClick = {
                    expanded = false
                    onLanguageSelected(null)
                },
            )
            for (language in AppLanguage.entries) {
                DropdownMenuItem(
                    text = { Text(language.nativeName) },
                    trailingIcon = if (selected == language) checkIcon else null,
                    onClick = {
                        expanded = false
                        onLanguageSelected(language)
                    },
                )
            }
        }
    }
}
