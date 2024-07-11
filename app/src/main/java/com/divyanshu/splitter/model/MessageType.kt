package com.divyanshu.splitter.model

sealed class MessageType {
    data class Info(val msg: String) : MessageType()
    data class MessageByMe(val msg: String) : MessageType()
    data class MessageByPeer(val msg: String) : MessageType()
    data object ConnectedToPeer : MessageType()
}
