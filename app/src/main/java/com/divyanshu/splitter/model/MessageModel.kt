package com.divyanshu.splitter.model

public data class MessageModel(
    // TODO: Make it enum and refactor it @whattadarsh
    val type: String,
    val name: String? = null,
    val target: String? = null,
    val data: Any? = null
)