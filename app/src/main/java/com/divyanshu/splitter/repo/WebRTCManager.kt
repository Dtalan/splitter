package com.divyanshu.splitter.repo

import com.divyanshu.splitter.common.utils.ContextProvider
import com.divyanshu.splitter.common.utils.LogUtil
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

class WebRTCManager(
    private var target: String,
    private val socketConnection: SocketConnection,
    private val userName: String,
) : PeerConnection.Observer {
    private val TAG = WebRTCManager::class.java.toString()

    private val scope = CoroutineScope(Dispatchers.IO)
    private val _messageStream = MutableSharedFlow<MessageType>()
    val messageStream: SharedFlow<MessageType>
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

    fun updateTarget(name: String) {
        target = name
    }

    private fun initializePeerConnectionFactory() {
        val options = PeerConnectionFactory
            .InitializationOptions
            .builder(ContextProvider.getContext())
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
                LogUtil.d(TAG, "data channel onBufferedAmountChange: ")
            }

            override fun onStateChange() {
                LogUtil.d(TAG, "data channel onStateChange ")
            }

            override fun onMessage(buffer: DataChannel.Buffer?) {
                LogUtil.d(TAG, "onMessage: at line 86")
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
        LogUtil.d(TAG, "Received message: $message")
        scope.launch {
            if (message.isEmpty()) return@launch
            _messageStream.emit(
                MessageType.MessageByPeer(
                    message
                )
            )
        }
    }

    fun createOffer(from: String, target: String) {
        LogUtil.d(TAG, "user is available creating offer")
        val sdpObserver = object : SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                peerConnection.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(desc: SessionDescription?) {
                        LogUtil.d(
                            TAG,
                            "onCreateSuccess: .... using socket to notify peer"
                        )
                    }

                    override fun onSetSuccess() {
                        LogUtil.d(TAG, "onSetSuccess: ")
                        // IMP
                        // SDP: save this
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
                        LogUtil.d(TAG, "error in creating offer $error")
                    }

                    override fun onSetFailure(error: String?) {
                        LogUtil.d(TAG, "onSetFailure: err-> $error")
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

    fun addIceCandidate(p0: IceCandidate?) {
        peerConnection.addIceCandidate(p0)
    }

    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
    }

    // CHECK on this
    // IMP
    override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
        when (newState) {
            PeerConnection.IceConnectionState.CONNECTED,
            PeerConnection.IceConnectionState.COMPLETED -> {
                // Peers are connected
                LogUtil.d(TAG, "ICE Connection State: Connected ")
                scope.launch {
                    _messageStream.emit(
                        MessageType.ConnectedToPeer
                    )
                }
            }

            else -> {
                // Peers are not connected
                LogUtil.d(TAG, "ICE Connection State: not Connected")
            }
        }
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
    }


    // Ice Candidate
    override fun onIceCandidate(p0: IceCandidate?) {
        // IMP
        // ICE Candidate
        LogUtil.d(TAG, "onIceCandidate called ....")
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
        LogUtil.d(TAG, "onDataChannel: called for peers")
        p0!!.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(p0: Long) {
            }

            override fun onStateChange() {
            }

            override fun onMessage(p0: DataChannel.Buffer?) {
                LogUtil.d(TAG, "onMessage: at line 196")
                consumeDataChannelData(p0)
            }
        })
    }

    override fun onRenegotiationNeeded() {
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        TODO("Not yet implemented")
    }

    // IMP
    fun onRemoteSessionReceived(session: SessionDescription) {
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

    fun answerToOffer(lTarget: String?) {
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

    fun sendMessage(msg: String) {
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
