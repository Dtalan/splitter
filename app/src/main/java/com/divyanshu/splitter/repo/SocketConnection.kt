package com.divyanshu.splitter.repo

import android.util.Log
import com.divyanshu.splitter.model.MessageModel
import com.divyanshu.splitter.model.SocketEvents
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

private const val SOCKET_CONNECTION_TAG = "SocketConnection"

class SocketConnection {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val url = "ws://192.168.1.7:3000"

    private var webSocket: WebSocketClient? = null
    private val gson = Gson()

    private val _events = MutableSharedFlow<SocketEvents>()
    val event: SharedFlow<SocketEvents>
        get() = _events

    fun initSocket(
        username: String,
    ) {

        webSocket = object : WebSocketClient(URI(url)) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d(SOCKET_CONNECTION_TAG, "onOpen: ${Thread.currentThread()}")
                sendMessageToSocket(
                    MessageModel(
                        "store_user", username, null, null
                    )
                )
            }

            override fun onMessage(message: String?) {
                try {
                    Log.d(SOCKET_CONNECTION_TAG, "onMessage: $message")
                    emitEvent(
                        SocketEvents.OnSocketMessageReceived(
                            gson.fromJson(message, MessageModel::class.java)
                        )
                    )
                } catch (e: Exception) {
                    Log.d(SOCKET_CONNECTION_TAG, "onMessage: error -> $e")
                    emitEvent(
                        SocketEvents.ConnectionError(
                            e.message ?: "error in receiving messages from socket"
                        )
                    )
                    e.printStackTrace()
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(SOCKET_CONNECTION_TAG, "onClose: $reason")
                emitEvent(
                    SocketEvents.ConnectionChange(
                        isConnected = false,
                    )
                )
            }

            override fun onError(ex: Exception?) {
                Log.d(SOCKET_CONNECTION_TAG, "onError: $ex")
                emitEvent(
                    SocketEvents.ConnectionError(
                        ex?.message ?: "Socket exception"
                    )
                )
            }
        }
        webSocket?.connect()
    }

    private fun emitEvent(event: SocketEvents) {
        scope.launch {
            _events.emit(
                event
            )
        }
    }

    fun sendMessageToSocket(message: MessageModel) {
        try {
            Log.d(SOCKET_CONNECTION_TAG, "sendMessageToSocket: $message")
            webSocket?.send(Gson().toJson(message))
        } catch (e: Exception) {
            Log.d(SOCKET_CONNECTION_TAG, "sendMessageToSocket: $e")
        }
    }

}