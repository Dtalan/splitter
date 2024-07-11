package com.divyanshu.splitter.model

public sealed class MessageType {
    public data class Info(val msg: String) : MessageType()
    public data class MessageByMe(val msg: String) : MessageType()
    public data class MessageByPeer(val msg: String) : MessageType()
    public data object ConnectedToPeer : MessageType()
}
