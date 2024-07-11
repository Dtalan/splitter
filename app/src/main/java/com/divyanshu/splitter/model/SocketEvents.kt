package com.divyanshu.splitter.model

public sealed class SocketEvents {
    public data class OnSocketMessageReceived(val message: MessageModel) : SocketEvents()
    public data class ConnectionChange(val isConnected: Boolean) : SocketEvents()
    public data class ConnectionError(val error: String) : SocketEvents()
}