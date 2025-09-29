package net.ifmain.pinny.di

import net.ifmain.pinny.data.BookmarkRepositoryImpl
import net.ifmain.pinny.data.PlatformCapabilities
import net.ifmain.pinny.database.BookmarksDatabase
import net.ifmain.pinny.domain.port.BookmarkRepository
import net.ifmain.pinny.domain.usecase.AddBookmark
import net.ifmain.pinny.domain.usecase.ArchiveBookmark
import net.ifmain.pinny.domain.usecase.GetAllBookmarks
import net.ifmain.pinny.domain.usecase.SearchBookmarks
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun sharedModule(
    dbFactory: () -> BookmarksDatabase,
    platformCapabilities: PlatformCapabilities
) = module {
    single { Json { ignoreUnknownKeys = true } }
    single { dbFactory() }
    single { get<BookmarksDatabase>().bookmarksQueries }
    single<PlatformCapabilities> { platformCapabilities }
    single<BookmarkRepository> { BookmarkRepositoryImpl(get(), get(), get()) }

    single<() -> Long> { { get<PlatformCapabilities>().nowMillis() } }

    singleOf(::AddBookmark)
    singleOf(::ArchiveBookmark)
    singleOf(::SearchBookmarks)
    singleOf(::GetAllBookmarks)
}

