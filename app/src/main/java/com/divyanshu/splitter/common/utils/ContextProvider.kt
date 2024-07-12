package com.divyanshu.splitter.common.utils

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object ContextProvider{
    private lateinit var context: Context

    fun getContext(): Context {
        return context
    }

    fun setContext(context: Context){
        this.context = context
    }

}