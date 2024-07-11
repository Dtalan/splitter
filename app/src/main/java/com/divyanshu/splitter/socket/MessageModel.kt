package com.divyanshu.splitter.socket

data class MessageModel(
    // todo name it enum, and refactor it
    val type: String,
    val name: String? = null,
    val target: String? = null,
    val data: Any? = null
)