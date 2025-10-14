package net.ifmain.pinny.di

import net.ifmain.pinny.data.*
import net.ifmain.pinny.presentation.home.*
import net.ifmain.pinny.work.*
import org.koin.android.ext.koin.*
import org.koin.core.module.dsl.*
import org.koin.dsl.*

val androidAppModule = module {
    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get()) }
    single { HtmlMetadataParser() }
    single<MetadataSync> { AndroidMetadataSync(androidContext()) }
}
