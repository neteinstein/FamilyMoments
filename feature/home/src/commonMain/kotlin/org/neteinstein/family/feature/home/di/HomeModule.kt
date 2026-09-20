package org.neteinstein.family.feature.home.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.neteinstein.family.feature.home.HomeViewModel

val homeModule =
    module {
        viewModel { HomeViewModel(get(), get(), get(), get()) }
    }
