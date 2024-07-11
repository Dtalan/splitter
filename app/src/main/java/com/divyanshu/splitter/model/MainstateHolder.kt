package com.divyanshu.splitter.model

data class MainScreenState(
    val isConnectedToServer: Boolean = false,
    val isConnectToPeer: String? = null,
    val connectedAs: String = "",
    val messagesFromServer: List<MessageType> = emptyList(),
    val inComingRequestFrom: String = "",
    val isRtcEstablished: Boolean = false,
    val peerConnectionString: String = "",
) {
    companion object {
        fun forPreview(): MainScreenState {
            return MainScreenState(
                isConnectedToServer = true,
                isConnectToPeer = "Moto",
                connectedAs = "P6",
                messagesFromServer = listOf(
                    MessageType.Info("Connected to Server as P6"),
                    MessageType.Info("Connected to Peer Moto"),
                    MessageType.MessageByMe("Hello Moto Edge"),
                    MessageType.MessageByPeer("Hi P6, we have"),
                    MessageType.MessageByMe("Which Android OS version are you running???"),
                ),
            )
        }
    }
}
