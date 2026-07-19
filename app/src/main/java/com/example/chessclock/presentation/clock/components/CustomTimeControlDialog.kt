package com.example.chessclock.presentation.clock.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.chessclock.R
import com.example.chessclock.domain.clock.model.TimeControl

@Composable
fun CustomTimeControlDialog(
    onDismiss: () -> Unit,
    onConfirm: (TimeControl) -> Unit,  //
) {
    var minutes by remember { mutableStateOf("5") }
    var incrementSeconds by remember { mutableStateOf("0") }
    val minutesValue = minutes.toLongOrNull()
    val incrementValue = incrementSeconds.toLongOrNull()
    val valid = minutesValue != null && minutesValue in 1..180 &&
            incrementValue != null && incrementValue in 0..60
    
    val customName = stringResource(R.string.custom_time_control)

    AlertDialog(   //
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_time_control_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.custom_time_control_desc))  
                OutlinedTextField(
                    value = minutes,
                    onValueChange = { minutes = it.filter(Char::isDigit).take(3) },
                    label = { Text(stringResource(R.string.minutes_per_player)) },
                    supportingText = { Text(stringResource(R.string.minutes_range_hint)) },
                    isError = minutes.isNotEmpty() && minutesValue !in 1L..180L,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = incrementSeconds,
                    onValueChange = { incrementSeconds = it.filter(Char::isDigit).take(2) },
                    label = { Text(stringResource(R.string.increment_per_move)) },
                    supportingText = { Text(stringResource(R.string.seconds_range_hint)) },
                    isError = incrementSeconds.isNotEmpty() && incrementValue !in 0L..60L,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    onConfirm(
                        TimeControl(
                            name = customName,
                            baseMillis = minutesValue!! * 60_000,
                            incrementMillis = incrementValue!! * 1_000,
                        )
                    )
                },
            ) { Text(stringResource(R.string.apply)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
