package net.ifmain.pinny.di

import net.ifmain.pinny.data.HtmlMetadataParser
import net.ifmain.pinny.presentation.home.HomeViewModel
import net.ifmain.pinny.work.AndroidMetadataSync
import net.ifmain.pinny.work.MetadataSync
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val androidAppModule = module {
    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get()) }
    single { HtmlMetadataParser() }
    single<MetadataSync> { AndroidMetadataSync(androidContext()) }
}
