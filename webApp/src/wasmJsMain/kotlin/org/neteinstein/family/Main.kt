package org.neteinstein.family

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.neteinstein.family.di.doInitKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    doInitKoin()
    ComposeViewport {
        App()
    }
}
