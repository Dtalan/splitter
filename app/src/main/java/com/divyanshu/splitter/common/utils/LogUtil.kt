package com.divyanshu.splitter.common.utils

import android.nfc.Tag
import android.util.Log

// TODO: Implement in-app logger using Timber? by @divyanshu
object LogUtil{
    fun i(tag: String, message: String){
        Log.i("Splitter :: $tag", message)
    }
    fun d(tag: String, message: String){
        Log.d("Splitter :: $tag", message)
    }
    fun e(tag: String, message: String){
        Log.e("Splitter :: $tag", message)
    }
}