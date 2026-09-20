package org.neteinstein.family.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.neteinstein.family.ui.resources.Res
import org.neteinstein.family.ui.resources.cd_dismiss_install_app_banner
import org.neteinstein.family.ui.resources.install_app_banner_action
import org.neteinstein.family.ui.resources.install_app_banner_message

private const val DISMISSED_KEY = "install_app_banner_dismissed"
private const val LATEST_RELEASE_URL = "https://github.com/neteinstein/FamilyMoments/releases/latest"

@Composable
actual fun InstallAppBanner() {
    var dismissed by remember { mutableStateOf(isBannerDismissed()) }
    if (dismissed) return

    val uriHandler = LocalUriHandler.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(Res.string.install_app_banner_message),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { uriHandler.openUri(LATEST_RELEASE_URL) }) {
                Text(stringResource(Res.string.install_app_banner_action))
            }
            IconButton(
                onClick = {
                    dismissed = true
                    persistBannerDismissed()
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.cd_dismiss_install_app_banner),
                )
            }
        }
    }
}

private fun isBannerDismissed(): Boolean = readLocalStorageItem(DISMISSED_KEY) == "true"

private fun persistBannerDismissed() {
    writeLocalStorageItem(DISMISSED_KEY, "true")
}

private fun readLocalStorageItem(key: String): String? = js("localStorage.getItem(key)")

private fun writeLocalStorageItem(
    key: String,
    value: String,
): Unit = js("localStorage.setItem(key, value)")
