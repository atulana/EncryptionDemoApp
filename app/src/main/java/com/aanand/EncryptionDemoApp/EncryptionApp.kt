package com.aanand.EncryptionDemoApp

import android.app.Application
import com.aanand.EncryptionDemoApp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class EncryptionApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@EncryptionApp)
            modules(appModule)
        }
    }
}
