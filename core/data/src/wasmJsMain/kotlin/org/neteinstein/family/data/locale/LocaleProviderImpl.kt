package org.neteinstein.family.data.locale

import org.neteinstein.family.domain.repository.LocaleProvider

/**
 * Hardcoded for now rather than reading `navigator.language` - a real browser-language lookup is
 * left for a later pass, same interim choice as this module's other wasmJs actuals.
 */
actual class LocaleProviderImpl actual constructor() : LocaleProvider {
    actual override fun currentLanguageCode(): String = "en"
}
