package com.wonrax.bkstinfo

import android.app.Application
import com.facebook.stetho.Stetho
import com.wonrax.bkstinfo.models.DeviceUser
import com.wonrax.bkstinfo.network.OkHttpClientSingleton

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Enable network inspection
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
        }
        // Init singleton to enable cookies
        OkHttpClientSingleton.init(this)

        DeviceUser.init(this)
    }
}
