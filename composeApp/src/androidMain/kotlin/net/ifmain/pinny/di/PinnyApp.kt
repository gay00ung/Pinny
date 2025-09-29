package net.ifmain.pinny.di

import android.app.*
import app.cash.sqldelight.driver.android.*
import net.ifmain.pinny.data.*
import net.ifmain.pinny.database.*
import org.koin.android.ext.koin.*
import org.koin.core.context.*

class PinnyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val dbFactory = {
            val driver = AndroidSqliteDriver(
                schema = BookmarksDatabase.Schema,
                context = this,
                name = "pinny.db"
            )
            BookmarksDatabase(driver)
        }

        startKoin {
            androidContext(this@PinnyApp)
            modules(
                sharedModule(dbFactory, AndroidPlatformCapabilities()),
                androidAppModule
            )
        }
    }
}
