package com.divyanshu.splitter.presentation.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.divyanshu.splitter.common.utils.LogUtil
import com.divyanshu.splitter.model.IceCandidateModel
import com.divyanshu.splitter.model.MainActions
import com.divyanshu.splitter.model.MainScreenState
import com.divyanshu.splitter.repo.SocketConnection
import com.divyanshu.splitter.repo.WebRTCManager
import com.divyanshu.splitter.viewmodel.MainViewModel
import com.google.gson.Gson
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

object OfferJson {
    val sdp: String =
        "{\n    \"type\": \"OFFER\",    \"sdp\": \"v=0\no=- 6223384477986725203 2 IN IP4 127.0.0.1\ns=-\nt=0 0\na=group:BUNDLE 0\na=extmap-allow-mixed\na=msid-semantic: WMS\nm=application 9 UDP/DTLS/SCTP webrtc-datachannel\nc=IN IP4 0.0.0.0\na=ice-ice-ufrag:8Vnf\na=ice-pwd:7X0GbwLdq7RN8nQAMRlxtI5y\n\na=ice-options:trickle renomination\na=fingerprint:sha-256 99:13:A8:97:03:FF:40:44:AA:6A:76:66:39:92:DC:C1:BF:DD:44:9D:7A:E7:45:13:5F:35:B1:E3:9D:D2:BE:FC\na=setup:actpass\na=mid:0\na=sctp-port:5000\na=max-message-size:262144\"\n}"
    val ice: String =
        "{\n    \"sdpMLineIndex\":\"0\", \"sdpMid\": \"0\", \"sdpCandidate\": \"candidate:1909871170 1 udp 2122260223 192.168.1.11 37801 typ host generation 0 ufrag 8Vnf network-id 4 network-cost 10\"\n}"
}

@Composable
fun TestScreenContent(
    state: MainScreenState,
    dispatchAction: (MainActions) -> Unit = {},
) {
    val viewModel = viewModel(modelClass = MainViewModel::class.java)
    val TAG = "TestScreenContent"
    var sdp: String by remember {
        mutableStateOf(OfferJson.sdp)
    }
    var ice: String by remember {
        mutableStateOf(OfferJson.ice)
    }
    val gson = Gson()
    val scrollState = rememberScrollState()

    Box {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Text(
                text = "SDP\n$sdp",
//            onValueChange = {
//                sdp = gson.toJson(it)
//            },
//            placeholder = { Text(text = "SDP") }
            )
            Text(
                text = "ICE\n$ice",
//            onValueChange = { ice = it },
//            placeholder = { Text(text = "ICE") }
            )


        }

        Button(
            onClick = {
                LogUtil.d(TAG, "Connection Start")
                LogUtil.d(
                    TAG, "SDP: $sdp \nICE: $ice"
                )

                val session = SessionDescription(
                    SessionDescription.Type.ANSWER,
                    gson.toJson(sdp)
                )

                LogUtil.d(TAG, "onNewMessage: answer received $session")

                val socketConnection = SocketConnection()
                val rtcManager = WebRTCManager(
                    socketConnection = socketConnection,
                    userName = "A",
                    target = "Divyanshu",
                )
                rtcManager.onRemoteSessionReceived(session)

                // ICE
                val receivingCandidate: IceCandidateModel = gson.fromJson(
                    ice,
                    IceCandidateModel::class.java
                )
                LogUtil.d(TAG, "Ice candidate $receivingCandidate")

                rtcManager.addIceCandidate(
                    IceCandidate(
                        receivingCandidate.sdpMid,
                        Math.toIntExact(receivingCandidate.sdpMLineIndex.toLong()),
                        receivingCandidate.sdpCandidate
                    )
                )

                /// Send Msg
                rtcManager.sendMessage("TEST 101")
                LogUtil.d(TAG, "Connection End")
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.White,
                containerColor = Color(0xFFFA7070),
            ),
        ) {
            Text(text = "TEST")
        }
    }
}