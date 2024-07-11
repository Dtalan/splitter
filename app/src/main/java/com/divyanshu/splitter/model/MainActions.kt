package com.divyanshu.splitter.model

sealed class MainActions {
    data class ConnectAs(val name: String) : MainActions()
    data object AcceptIncomingConnection : MainActions()
    data class ConnectToUser(val name: String) : MainActions()
    data class SendChatMessage(val msg: String) : MainActions()
}