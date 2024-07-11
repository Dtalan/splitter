package com.divyanshu.splitter

import android.app.Application
import android.content.Context

public class TransferApp : Application() {

    public companion object {
        private lateinit var application: Application

        public fun getContext(): Context {
            return application
        }
    }

    override fun onCreate() {
        super.onCreate()
        application = this
    }

}