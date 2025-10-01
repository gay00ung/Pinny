package net.ifmain.pinny.di

import kotlinx.serialization.json.*
import net.ifmain.pinny.data.*
import net.ifmain.pinny.database.*
import net.ifmain.pinny.domain.port.*
import net.ifmain.pinny.domain.usecase.*
import org.koin.core.module.dsl.*
import org.koin.dsl.*

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
    singleOf(::DeleteBookmark)
}

