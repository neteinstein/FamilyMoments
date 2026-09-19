package org.neteinstein.family.data.di

import org.koin.core.module.Module
import org.koin.dsl.module

// The GitHub self-update feature is Android-only (APK sideloading has no Web equivalent).
actual val platformUpdateModule: Module = module {}
