package org.neteinstein.family.di

import org.koin.dsl.module
import org.neteinstein.family.data.di.dataModule
import org.neteinstein.family.feature.home.di.homeModule

val appModule = module {
    includes(dataModule, homeModule)
}
