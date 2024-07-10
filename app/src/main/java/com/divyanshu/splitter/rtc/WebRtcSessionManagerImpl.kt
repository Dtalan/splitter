package com.divyanshu.splitter.rtc

import android.content.Context
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import com.divyanshu.splitter.p2p.peer.SignalingClient
import com.divyanshu.splitter.p2p.peer.SignalingCommand
import com.divyanshu.splitter.p2p.peer.SplitterPeerConnection
import com.divyanshu.splitter.p2p.peer.SplitterPeerConnectionFactory
import com.divyanshu.splitter.p2p.peer.SplitterPeerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.SessionDescription

private const val ICE_SEPARATOR = '$'

val LocalWebRtcSessionManager: ProvidableCompositionLocal<WebRtcSessionManager> =
    staticCompositionLocalOf { error("WebRtcSessionManager was not initialized!") }


class WebRtcSessionManagerImpl(
    private val context: Context,
    override val signalingClient: SignalingClient,
    override val peerConnectionFactory: SplitterPeerConnectionFactory
) : WebRtcSessionManager {
//    private val logger by taggedLogger("Call:LocalWebRtcSessionManager")

    private val sessionManagerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)


    private var offer: String? = null

    private val peerConnection: SplitterPeerConnection by lazy {
        peerConnectionFactory.makePeerConnection(
            coroutineScope = sessionManagerScope,
            configuration = peerConnectionFactory.rtcConfig,
            type = SplitterPeerType.SUBSCRIBER,
            onIceCandidateRequest = { iceCandidate, _ ->
                signalingClient.sendCommand(
                    SignalingCommand.ICE,
                    "${iceCandidate.sdpMid}$ICE_SEPARATOR${iceCandidate.sdpMLineIndex}$ICE_SEPARATOR${iceCandidate.sdp}"
                )
            },
        )
    }


    init {
        sessionManagerScope.launch {
            signalingClient.signalingCommandFlow
                .collect { commandToValue ->
                    when (commandToValue.first) {
                        SignalingCommand.OFFER -> handleOffer(commandToValue.second)
                        SignalingCommand.ANSWER -> handleAnswer(commandToValue.second)
                        SignalingCommand.ICE -> handleIce(commandToValue.second)
                        else -> Unit
                    }
                }
        }
    }


    override fun onSessionScreenReady() {
//        peerConnection.connection.addTrack(localVideoTrack)
//        peerConnection.connection.addTrack(localAudioTrack)
        sessionManagerScope.launch {
            // sending local video track to show local video from start
//            _localVideoTrackFlow.emit(localVideoTrack)

            if (offer != null) {
                sendAnswer()
            } else {
                sendOffer()
            }
        }
    }


    override fun disconnect() {
        // dispose signaling clients and socket.
        signalingClient.dispose()
    }

    private suspend fun sendOffer() {
        val offer = peerConnection.createOffer().getOrThrow()
        val result = peerConnection.setLocalDescription(offer)
        result.onSuccess {
            signalingClient.sendCommand(SignalingCommand.OFFER, offer.description)
        }
//        logger.d { "[SDP] send offer: ${offer.stringify()}" }
    }


    private suspend fun sendAnswer() {
        peerConnection.setRemoteDescription(
            SessionDescription(SessionDescription.Type.OFFER, offer)
        )
        val answer = peerConnection.createAnswer().getOrThrow()
        val result = peerConnection.setLocalDescription(answer)
        result.onSuccess {
            signalingClient.sendCommand(SignalingCommand.ANSWER, answer.description)
        }
//        logger.d { "[SDP] send answer: ${answer.stringify()}" }
    }

    private fun handleOffer(sdp: String) {
//        logger.d { "[SDP] handle offer: $sdp" }
        offer = sdp
    }

    private suspend fun handleAnswer(sdp: String) {
//        logger.d { "[SDP] handle answer: $sdp" }
        peerConnection.setRemoteDescription(
            SessionDescription(SessionDescription.Type.ANSWER, sdp)
        )
    }

    private suspend fun handleIce(iceMessage: String) {
        val iceArray = iceMessage.split(ICE_SEPARATOR)
        peerConnection.addIceCandidate(
            IceCandidate(
                iceArray[0],
                iceArray[1].toInt(),
                iceArray[2]
            )
        )
    }
}
