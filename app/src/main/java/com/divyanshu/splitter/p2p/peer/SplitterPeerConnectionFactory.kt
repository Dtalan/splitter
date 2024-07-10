package com.divyanshu.splitter.p2p.peer

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import org.webrtc.IceCandidate
import org.webrtc.Logging
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory

class SplitterPeerConnectionFactory(
    private val context: Context
) {

    // rtcConfig contains STUN and TURN servers list
    val rtcConfig = PeerConnection.RTCConfiguration(
        arrayListOf(
            // adding google's standard server
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )
    ).apply {
        // it's very important to use new unified sdp semantics PLAN_B is deprecated
        sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
    }

    private val factory by lazy {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setInjectableLogger({ message, severity, label ->
                    when (severity) {
                        Logging.Severity.LS_VERBOSE -> {
                            Log.v("ADARSH", "[onLogMessage] label: $label, message: $message")
                        }

                        Logging.Severity.LS_INFO -> {
                            Log.i("ADARSH", "[onLogMessage] label: $label, message: $message")
                        }

                        Logging.Severity.LS_WARNING -> {
                            Log.w("ADARSH", "[onLogMessage] label: $label, message: $message")
                        }

                        Logging.Severity.LS_ERROR -> {
                            Log.e("ADARSH", "[onLogMessage] label: $label, message: $message")
                        }

                        Logging.Severity.LS_NONE -> {
                            Log.d("ADARSH", "[onLogMessage] label: $label, message: $message")
                        }

                        else -> {}
                    }
                }, Logging.Severity.LS_VERBOSE)
                .createInitializationOptions()

        )

        PeerConnectionFactory.builder()
            .setOptions(PeerConnectionFactory.Options())
            .createPeerConnectionFactory()
    }


    fun makePeerConnection(
        coroutineScope: CoroutineScope,
        configuration: PeerConnection.RTCConfiguration,
        type: SplitterPeerType,
        onStreamAdded: ((MediaStream) -> Unit)? = null,
        onNegotiationNeeded: ((SplitterPeerConnection, SplitterPeerType) -> Unit)? = null,
        onIceCandidateRequest: ((IceCandidate, SplitterPeerType) -> Unit)? = null,
    ): SplitterPeerConnection {
        val peerConnection = SplitterPeerConnection(
            coroutineScope = coroutineScope,
            type = type,
            onStreamAdded = onStreamAdded,
            onNegotiationNeeded = onNegotiationNeeded,
            onIceCandidate = onIceCandidateRequest,
        )
        val connection = makePeerConnectionInternal(
            configuration = configuration,
            observer = peerConnection
        )
        return peerConnection.apply { initialize(connection) }
    }

    private fun makePeerConnectionInternal(
        configuration: PeerConnection.RTCConfiguration,
        observer: PeerConnection.Observer?
    ): PeerConnection {
        return requireNotNull(
            factory.createPeerConnection(
                configuration,
                observer
            )
        )
    }
}