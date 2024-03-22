package data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import data.DataBaseEnum
import org.gutierrethiago.raxa_football_kotlin_multi.database.AppDatabase
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val isDebug = true// or you can use BuildKonfig.isDebug to setup your logic
        val parentFolder = if (isDebug) {
            File(System.getProperty("java.io.tmpdir"))
        } else {
            File(System.getProperty("user.home") + "/MyFancyApp")
        }
        if (!parentFolder.exists()) {
            parentFolder.mkdirs()
        }
        val databasePath = if (isDebug) {
            File(System.getProperty("java.io.tmpdir"), DataBaseEnum.DB_NAME.value)
        } else {
            File(parentFolder, DataBaseEnum.DB_NAME.value)
        }
        return JdbcSqliteDriver(url = "jdbc:sqlite:${databasePath.absolutePath}").also { driver ->
            AppDatabase.Schema.create(driver = driver)
        }
    }
}