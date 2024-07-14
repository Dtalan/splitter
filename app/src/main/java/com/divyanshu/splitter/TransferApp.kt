package com.divyanshu.splitter

import android.app.Application
import android.content.Context
import com.divyanshu.splitter.common.utils.ContextProvider

class TransferApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ContextProvider.setContext(this)
    }
}