package com.ssnlva

import android.app.Application
import com.ssnlva.di.audioModule
import com.ssnlva.di.domainModule
import com.ssnlva.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PiAInoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PiAInoApplication)
            modules(domainModule, audioModule, presentationModule)
        }
    }
}
