package com.divyanshu.splitter.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.divyanshu.splitter.presentation.widgets.DialogForIncomingRequest
import com.divyanshu.splitter.presentation.widgets.HomeScreenContent
import com.divyanshu.splitter.model.MainActions
import com.divyanshu.splitter.model.MainOneTimeEvents
import com.divyanshu.splitter.viewmodel.MainViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@Composable
public fun HomeScreen() {
    val viewModel = viewModel(modelClass = MainViewModel::class.java)
    val state by viewModel.state.collectAsState()
    val events = rememberFlowWithLifecycle(flow = viewModel.oneTimeEvents)
    var showIncomingRequestDialog by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(
        key1 = events,
        block = {
            events.collectLatest {
                when (it) {
                    is MainOneTimeEvents.GotInvite -> {
                        showIncomingRequestDialog = true
                    }
                }
            }
        },
    )
    if (showIncomingRequestDialog) {
        DialogForIncomingRequest(
            onDismiss = {
                showIncomingRequestDialog = false
            },
            onAccept = {
                viewModel.dispatchAction(
                    MainActions.AcceptIncomingConnection
                )
                showIncomingRequestDialog = false
            },
            inviteFrom = state.inComingRequestFrom,
        )
    }
    HomeScreenContent(
        state = state,
        dispatchAction = {
            viewModel.dispatchAction(it)
        },
    )
}

@Composable
private fun <T> rememberFlowWithLifecycle(
    flow: Flow<T>,
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
): Flow<T> = remember(flow, lifecycle, minActiveState) {
    flow.flowWithLifecycle(
        lifecycle = lifecycle,
        minActiveState = minActiveState,
    )
}
