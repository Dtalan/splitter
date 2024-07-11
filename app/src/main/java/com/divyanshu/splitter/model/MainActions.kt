package com.divyanshu.splitter.model

public sealed class MainActions {
    public data class ConnectAs(val name: String) : MainActions()
    public data object AcceptIncomingConnection : MainActions()
    public data class ConnectToUser(val name: String) : MainActions()
    public data class SendChatMessage(val msg: String) : MainActions()
}