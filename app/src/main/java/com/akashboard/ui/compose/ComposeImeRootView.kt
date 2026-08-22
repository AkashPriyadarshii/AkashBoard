package com.akashboard.ui.compose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AbstractComposeView
import com.akashboard.AkashBoardIME

class ComposeImeRootView(
    context: Context,
    val ime: AkashBoardIME
) : AbstractComposeView(context) {

    @Composable
    override fun Content() {
        AkashBoardRoot(ime)
    }
}
