package net.ifmain.pinny.di

import net.ifmain.pinny.presentation.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val androidAppModule = module {
    viewModel { HomeViewModel(get(), get(), get(), get()) }
}
