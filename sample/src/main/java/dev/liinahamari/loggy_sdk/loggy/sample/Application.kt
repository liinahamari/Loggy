package dev.liinahamari.loggy_sdk.loggy.sample

import dev.liinahamari.loggy_sdk.Loggy
import dev.liinahamari.loggy_sdk.helper.createFileIfNotExist

const val DEBUG_LOGS_DIR = "TempLogs"
const val DEBUG_LOGS_STORAGE_FILE = "tape.log"
const val MY_EMAIL = "l1bills@protonmail.com"
const val USER_ID = "dummy_id"

@Suppress("unused")
class Application: android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        Loggy.init(
            application = this,
            integratorEmail = MY_EMAIL,
            userId = USER_ID
        )
    }
}