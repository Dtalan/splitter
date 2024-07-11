package com.divyanshu.splitter.model

sealed class MainOneTimeEvents {
    data object GotInvite : MainOneTimeEvents()
}