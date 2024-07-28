package com.divyanshu.splitter

import android.app.Application
import com.divyanshu.splitter.common.utils.ContextProvider
import com.google.firebase.FirebaseApp

class TransferApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ContextProvider.setContext(this)
        FirebaseApp.initializeApp(this)
    }
}
