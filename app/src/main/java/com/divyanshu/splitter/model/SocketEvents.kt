package com.divyanshu.splitter.model

sealed class SocketEvents {
    data class OnSocketMessageReceived(val message: MessageModel) : SocketEvents()
    data class ConnectionChange(val isConnected: Boolean) : SocketEvents()
    data class ConnectionError(val error: String) : SocketEvents()
}