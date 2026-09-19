package org.neteinstein.family.data.locale

import org.neteinstein.family.domain.repository.LocaleProvider
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

actual class LocaleProviderImpl actual constructor() : LocaleProvider {
    actual override fun currentLanguageCode(): String = NSLocale.currentLocale.languageCode
}
