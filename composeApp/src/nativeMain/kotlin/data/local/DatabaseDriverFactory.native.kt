package data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import data.DataBaseEnum
import org.gutierrethiago.raxa_football_kotlin_multi.database.AppDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = AppDatabase.Schema,
            name = DataBaseEnum.DB_NAME.value
        )    }
}