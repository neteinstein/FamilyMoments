package org.neteinstein.family.data.locale

import org.neteinstein.family.domain.repository.LocaleProvider

/**
 * `navigator.language` (e.g. "en-US", "pt-BR") is the browser's own reported UI language - the
 * closest wasmJs equivalent to Android's `Locale.getDefault()`/iOS's `NSLocale.currentLocale`.
 * Read via an inline `js("...")` snippet (Kotlin/Wasm JS interop) rather than the `kotlinx-browser`
 * library, since a single global property read doesn't need a whole extra dependency.
 */
actual class LocaleProviderImpl actual constructor() : LocaleProvider {
    actual override fun currentLanguageCode(): String = browserLanguage().substringBefore('-')
}

private fun browserLanguage(): String = js("navigator.language")
