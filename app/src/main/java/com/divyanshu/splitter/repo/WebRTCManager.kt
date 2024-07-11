package com.divyanshu.splitter.repo

import android.util.Log
import com.divyanshu.splitter.TransferApp
import com.divyanshu.splitter.model.MessageModel
import com.divyanshu.splitter.model.MessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import java.nio.ByteBuffer

private const val WEB_RTC_MANAGER_TAG = "WebRtcManager"

public class WebRTCManager(
    private var target: String,
    private val socketConnection: SocketConnection,
    private val userName: String,
) : PeerConnection.Observer {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val _messageStream = MutableSharedFlow<MessageType>()
    public val messageStream: SharedFlow<MessageType>
        get() = _messageStream

    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:iphone-stun.strato-iphone.de:3478")
            .createIceServer(),
        PeerConnection.IceServer("stun:openrelay.metered.ca:80"),
        PeerConnection.IceServer(
            "turn:openrelay.metered.ca:80",
            "openrelayproject",
            "openrelayproject"
        ),
        PeerConnection.IceServer(
            "turn:openrelay.metered.ca:443",
            "openrelayproject",
            "openrelayproject"
        ),
        PeerConnection.IceServer(
            "turn:openrelay.metered.ca:443?transport=tcp",
            "openrelayproject",
            "openrelayproject"
        ),
    )
    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private lateinit var peerConnection: PeerConnection
    private lateinit var dataChannel: DataChannel

    init {
        initializePeerConnectionFactory()
        createPeerConnection()
        createDataChannel("localDataChannel")
    }

    public fun updateTarget(name: String) {
        target = name
    }

    private fun initializePeerConnectionFactory() {
        val options = PeerConnectionFactory
            .InitializationOptions
            .builder(TransferApp.getContext())
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        val peerConnectionFactoryOptions = PeerConnectionFactory.Options()

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(peerConnectionFactoryOptions)
            .createPeerConnectionFactory()
    }

    private fun createPeerConnection() {
        peerConnection =
            peerConnectionFactory.createPeerConnection(iceServers, this)!!
    }

    private fun createDataChannel(label: String) {
        val init = DataChannel.Init()
        dataChannel = peerConnection.createDataChannel(label, init)
        dataChannel.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(amount: Long) {
                Log.d(WEB_RTC_MANAGER_TAG, "data channel onBufferedAmountChange: ")
            }

            override fun onStateChange() {
                Log.d(WEB_RTC_MANAGER_TAG, "data channel onStateChange ")
            }

            override fun onMessage(buffer: DataChannel.Buffer?) {
                Log.d(WEB_RTC_MANAGER_TAG, "onMessage: at line 86")
                consumeDataChannelData(buffer)
            }
        })
    }

    private fun consumeDataChannelData(buffer: DataChannel.Buffer?) {
        buffer ?: return
        val data = buffer.data
        val bytes = ByteArray(data.capacity())
        data.get(bytes)
        val message = String(bytes, Charsets.UTF_8)
        // Handle the received message
        Log.d(WEB_RTC_MANAGER_TAG, "Received message: $message")
        scope.launch {
            if (message.isEmpty()) return@launch
            _messageStream.emit(
                MessageType.MessageByPeer(
                    message
                )
            )
        }
    }

    public fun createOffer(from: String, target: String) {
        Log.d(WEB_RTC_MANAGER_TAG, "user is available creating offer")
        val sdpObserver = object : SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                peerConnection.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(desc: SessionDescription?) {
                        Log.d(
                            WEB_RTC_MANAGER_TAG,
                            "onCreateSuccess: .... using socket to notify peer"
                        )
                    }

                    override fun onSetSuccess() {
                        Log.d(WEB_RTC_MANAGER_TAG, "onSetSuccess: ")
                        val offer = hashMapOf(
                            "sdp" to desc?.description,
                            "type" to desc?.type
                        )

                        socketConnection.sendMessageToSocket(
                            MessageModel(
                                "create_offer", from, target, offer
                            )
                        )
                    }

                    override fun onCreateFailure(error: String?) {
                        Log.d(WEB_RTC_MANAGER_TAG, "error in creating offer $error")
                    }

                    override fun onSetFailure(error: String?) {
                        Log.d(WEB_RTC_MANAGER_TAG, "onSetFailure: err-> $error")
                    }
                }, desc)
                // Send offer to signaling server
                // Signaling server will forward it to the other peer
                // Upon receiving answer, set it as remote description
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {}
            override fun onSetFailure(error: String?) {}
        }

        val mediaConstraints = MediaConstraints()
        peerConnection.createOffer(sdpObserver, mediaConstraints)
    }

    public fun addIceCandidate(p0: IceCandidate?) {
        peerConnection.addIceCandidate(p0)
    }

    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
    }

    override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
        when (newState) {
            PeerConnection.IceConnectionState.CONNECTED,
            PeerConnection.IceConnectionState.COMPLETED -> {
                // Peers are connected
                Log.d(WEB_RTC_MANAGER_TAG, "ICE Connection State: Connected ")
                scope.launch {
                    _messageStream.emit(
                        MessageType.ConnectedToPeer
                    )
                }
            }

            else -> {
                // Peers are not connected
                Log.d(WEB_RTC_MANAGER_TAG, "ICE Connection State: not Connected")
            }
        }
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
    }

    override fun onIceCandidate(p0: IceCandidate?) {
        Log.d(WEB_RTC_MANAGER_TAG, "onIceCandidate called ....")
        addIceCandidate(p0)
        val candidate = hashMapOf(
            "sdpMid" to p0?.sdpMid,
            "sdpMLineIndex" to p0?.sdpMLineIndex,
            "sdpCandidate" to p0?.sdp
        )
        socketConnection.sendMessageToSocket(
            MessageModel("ice_candidate", userName, target, candidate)
        )
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
    }

    override fun onAddStream(p0: MediaStream?) {
    }

    override fun onRemoveStream(p0: MediaStream?) {
    }

    override fun onDataChannel(p0: DataChannel?) {
        Log.d(WEB_RTC_MANAGER_TAG, "onDataChannel: called for peers")
        p0!!.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(p0: Long) {
            }

            override fun onStateChange() {
            }

            override fun onMessage(p0: DataChannel.Buffer?) {
                Log.d(WEB_RTC_MANAGER_TAG, "onMessage: at line 196")
                consumeDataChannelData(p0)
            }
        })
    }

    override fun onRenegotiationNeeded() {
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        TODO("Not yet implemented")
    }

    public fun onRemoteSessionReceived(session: SessionDescription) {
        peerConnection.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {
            }

            override fun onSetSuccess() {
            }

            override fun onCreateFailure(p0: String?) {
            }

            override fun onSetFailure(p0: String?) {
            }
        }, session)
    }

    public fun answerToOffer(lTarget: String?) {
        val constraints = MediaConstraints()
        peerConnection.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                peerConnection.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {
                    }

                    override fun onSetSuccess() {
                        val answer = hashMapOf(
                            "sdp" to desc?.description,
                            "type" to desc?.type
                        )
                        socketConnection.sendMessageToSocket(
                            MessageModel(
                                "create_answer", userName, lTarget, answer
                            )
                        )
                    }

                    override fun onCreateFailure(p0: String?) {
                    }

                    override fun onSetFailure(p0: String?) {
                    }

                }, desc)
            }

            override fun onSetSuccess() {
            }

            override fun onCreateFailure(p0: String?) {
            }

            override fun onSetFailure(p0: String?) {
            }
        }, constraints)
    }

    public fun sendMessage(msg: String) {
        val buffer = ByteBuffer.wrap(msg.toByteArray(Charsets.UTF_8))
        val binaryData = DataChannel.Buffer(buffer, false)
        scope.launch {
            if (msg.isEmpty()) return@launch
            _messageStream.emit(
                MessageType.MessageByMe(msg)
            )
        }
        dataChannel.send(binaryData)
    }
}