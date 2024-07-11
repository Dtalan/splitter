package com.divyanshu.splitter.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.divyanshu.splitter.common.theme.SplitterTheme

@Composable
public fun DialogForIncomingRequest(
    onDismiss: () -> Unit = {},
    onAccept: () -> Unit = {},
    inviteFrom: String,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White,
                )
                .padding(
                    8.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "You got invite from $inviteFrom")
            Row(
                modifier = Modifier
                    .padding(
                        horizontal = 20.dp,
                    )
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.padding(
                        vertical = 10.dp,
                    ),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.White,
                        containerColor = Color(0xFFFA7070),
                    ),
                ) {
                    Text(text = "Cancel")
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.padding(
                        vertical = 10.dp,
                    ),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.White,
                        containerColor = Color(0xFFFA7070),
                    ),
                ) {
                    Text(text = "Accept")
                }
            }
        }
    }
}

@Preview
@Composable
private fun DialogForIncomingRequestPreview() {
    SplitterTheme {
        DialogForIncomingRequest(
            onAccept = {},
            onDismiss = {},
            inviteFrom = "Shah Rukh Khan"
        )
    }
}
