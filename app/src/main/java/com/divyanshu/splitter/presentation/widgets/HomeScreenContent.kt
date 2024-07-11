package com.divyanshu.splitter.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.divyanshu.splitter.model.MainActions
import com.divyanshu.splitter.model.MainScreenState
import com.divyanshu.splitter.model.MessageType
import com.divyanshu.splitter.common.theme.SplitterTheme

@Composable
fun HomeScreenContent(
    state: MainScreenState,
    dispatchAction: (MainActions) -> Unit = {},
) {
    var yourName by remember {
        mutableStateOf("")
    }
    var connectTo by remember {
        mutableStateOf("")
    }
    var chatMessage by remember {
        mutableStateOf("")
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = Color(0xFFFEFDED)
            ),
    ) {
        LazyColumn(
            content = {
                item {
                    if (state.peerConnectionString.isEmpty()) {
                        Text(
                            text = if (state.isConnectedToServer)
                                "Connected to server as ${state.connectedAs}"
                            else "Not connected to server",
                            modifier = Modifier
                                .align(
                                    Alignment.TopCenter,
                                )
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFFD20062),
                                )
                                .padding(
                                    10.dp
                                ),
                            color = Color.White,
                        )
                    } else {
                        Text(
                            text = state.peerConnectionString,
                            modifier = Modifier
                                .align(
                                    Alignment.TopCenter,
                                )
                                .fillMaxWidth()
                                .background(
                                    Color(0xFFD20062),
                                )
                                .padding(
                                    10.dp
                                ),
                            color = Color.White,
                        )
                    }
                }
                items(state.messagesFromServer.size) {
                    val current = state.messagesFromServer[it]
                    when (current) {
                        is MessageType.Info -> {
                            Text(
                                text = current.msg,
                                modifier = Modifier
                                    .padding(
                                        top = 10.dp,
                                        start = 10.dp,
                                    )
                                    .fillMaxWidth()
                            )
                        }

                        is MessageType.MessageByMe -> {
                            Row(
                                modifier = Modifier
                                    .padding(
                                        top = 10.dp,
                                        start = 10.dp,
                                    )
                                    .fillMaxWidth(),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                                Text(
                                    text = current.msg,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .background(
                                            color = Color(0xFF240A34),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .padding(
                                            8.dp,
                                        ),
                                    color = Color.White,
                                )
                            }
                        }

                        is MessageType.MessageByPeer -> {
                            Row(
                                modifier = Modifier
                                    .padding(
                                        top = 10.dp,
                                        start = 10.dp,
                                    )
                                    .fillMaxWidth(),
                            ) {
                                Text(
                                    text = current.msg,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .background(
                                            color = Color(0xFFFA7070),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .padding(
                                            8.dp,
                                        ),
                                    color = Color.White,
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                            }
                        }

                        else -> {}
                    }
                }
            },
        )
        Column(
            modifier = Modifier.align(
                Alignment.BottomCenter,
            ),
        ) {
            if (state.isRtcEstablished) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        top = 10.dp,
                        bottom = 10.dp,
                        start = 10.dp,
                    ),
                ) {
                    TextField(
                        modifier = Modifier.weight(1f),
                        value = chatMessage,
                        onValueChange = {
                            chatMessage = it
                        },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color(0xFFFA7070),
                            unfocusedContainerColor = Color(0xFFFA7070),
                        ),
                        shape = RoundedCornerShape(15.dp),
                    )
                    Button(
                        onClick = {
                            dispatchAction(
                                MainActions.SendChatMessage(chatMessage)
                            )
                        },
                        modifier = Modifier.padding(
                            start = 10.dp,
                            end = 10.dp,
                        ),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.White,
                            containerColor = Color(0xFFFA7070),
                        ),
                    ) {
                        Text(text = "Chat")
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        start = 10.dp,
                    ),
                ) {
                    TextField(
                        modifier = Modifier.weight(1f),
                        value = if (state.connectedAs.isNotEmpty()) {
                            connectTo
                        } else {
                            yourName
                        },
                        onValueChange = {
                            if (state.connectedAs.isNotEmpty()) {
                                connectTo = it
                            } else {
                                yourName = it
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color(0xFFFA7070),
                            unfocusedContainerColor = Color(0xFFFA7070),
                        ),
                        shape = RoundedCornerShape(15.dp),
                    )
                    Button(
                        onClick = {
                            if (state.connectedAs.isNotEmpty()) {
                                dispatchAction(
                                    MainActions.ConnectToUser(connectTo)
                                )
                            } else {
                                dispatchAction(
                                    MainActions.ConnectAs(yourName)
                                )
                            }
                        },
                        modifier = Modifier.padding(
                            start = 10.dp,
                            end = 10.dp,
                        ),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.White,
                            containerColor = Color(0xFFFA7070),
                        ),
                    ) {
                        Text(text = "GO")
                    }
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenContentPreview() {
    SplitterTheme {
        val state by remember {
            mutableStateOf(MainScreenState.forPreview())
        }
        HomeScreenContent(
            state = state,
        )
    }
}