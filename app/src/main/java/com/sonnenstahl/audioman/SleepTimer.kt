package com.sonnenstahl.audioman

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sonnenstahl.audioman.ui.theme.DarkPlotBackGround
import com.sonnenstahl.audioman.ui.theme.LightPlotBackGround
import com.sonnenstahl.audioman.ui.theme.LightTeal
import com.sonnenstahl.audioman.ui.theme.Teal

@Composable
fun SleepTimer(
    context: Context,
    onDismissRequest: () -> Unit,
) {
    val darkMode = isSystemInDarkTheme()
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            backgroundColor = if (darkMode) LightPlotBackGround else DarkPlotBackGround,
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp,
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Adjust Sleep Timer",
                    style = MaterialTheme.typography.h6,
                )

                TimerUi(
                    context,
                    initialMinutes = 10,
                    handleColor = if (darkMode) LightPlotBackGround else DarkPlotBackGround,
                    inactiveBarColor = Color.LightGray,
                    activeBarColor = if (darkMode) Teal else LightTeal,
                    modifier = Modifier.size(250.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray),
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}
