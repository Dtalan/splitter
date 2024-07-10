package com.divyanshu.splitter

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.divyanshu.splitter.driveInteractions.DriveInteraction
import com.divyanshu.splitter.p2p.peer.SignalingClient
import com.divyanshu.splitter.p2p.peer.SplitterPeerConnectionFactory
import com.divyanshu.splitter.rtc.LocalWebRtcSessionManager
import com.divyanshu.splitter.rtc.WebRtcSessionManager
import com.divyanshu.splitter.rtc.WebRtcSessionManagerImpl
import com.divyanshu.splitter.ui.theme.SplitterTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 0)

        val sessionManager: WebRtcSessionManager = WebRtcSessionManagerImpl(
            context = this,
            signalingClient = SignalingClient(),
            peerConnectionFactory = SplitterPeerConnectionFactory(this)
        )

        setContent {
            SplitterTheme {
                CompositionLocalProvider(LocalWebRtcSessionManager provides sessionManager) {
                    val state by sessionManager.signalingClient.sessionStateFlow.collectAsState()
                    HomeScreen()
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    var mUrl by remember { mutableStateOf("Hello") }
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = mUrl,
                onValueChange = { mUrl = it },
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Button(onClick = {
                val driveInteraction = DriveInteraction()
                driveInteraction.accessDrive(mUrl)
            }) {
                Text("Check Access")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SplitterTheme {
        HomeScreen()
    }
}