package com.divyanshu.splitter.presentation.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.divyanshu.splitter.common.theme.SplitterTheme
import com.divyanshu.splitter.presentation.screens.HomeScreen

public class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplitterTheme {
                HomeScreen()
            }
        }
    }
}
