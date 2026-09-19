package org.neteinstein.family.data.locale

import org.neteinstein.family.domain.repository.LocaleProvider

/** Platform-specific process/OS locale lookup - see each target's actual for details. */
expect class LocaleProviderImpl() : LocaleProvider {
    override fun currentLanguageCode(): String
}
