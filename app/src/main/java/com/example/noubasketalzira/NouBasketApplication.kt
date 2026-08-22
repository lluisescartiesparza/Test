package com.example.noubasketalzira

import android.app.Application
import com.example.noubasketalzira.core.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class NouBasketApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@NouBasketApplication)
            modules(appModule)
        }
    }
}
