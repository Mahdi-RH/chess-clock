package com.example.chessclock.presentation.clock.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import com.example.chessclock.R
import com.example.chessclock.domain.clock.model.Player
import com.example.chessclock.presentation.theme.ClockGreen
import com.example.chessclock.presentation.theme.ClockInactive
import com.example.chessclock.presentation.theme.ClockRed
import com.example.chessclock.presentation.theme.Dimens
import com.example.chessclock.presentation.theme.Spacing

@Composable
 fun PlayerClock(
    player: Player,
    formattedTime: String,
    moveCount: Int,
    isActive: Boolean,
    isFinished: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = when {
        isFinished -> ClockRed
        isActive -> ClockGreen
        else -> ClockInactive
    }
    val label = when {
        isFinished -> stringResource(R.string.time_out)  
        isActive -> stringResource(R.string.tap_to_end_turn)
        else -> {
            val playerNumber = if (player == Player.ONE) 1 else 2
            stringResource(R.string.player_label, playerNumber)
        } 
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.extraSmall)
            .background(background, RoundedCornerShape(Spacing.extraLarge))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = Spacing.large, vertical = Spacing.small),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.82f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            AutoResizingText(
                text = formattedTime,
                color = Color.White,
                targetTextSize = Dimens.clockFontSize,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f, fill = false)
            )
            Text(
                text = pluralStringResource(R.plurals.move_count, moveCount, moveCount),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.72f),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun AutoResizingText(
    text: String,
    targetTextSize: TextUnit,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    style: TextStyle = LocalTextStyle.current,
) {
    val configuration = LocalConfiguration.current  //
    var textSize by remember(targetTextSize, configuration.orientation) { //
        mutableStateOf(targetTextSize)
    }

    Text(
        text = text,
        color = color,
        fontSize = textSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        style = style,
        maxLines = 1,
        softWrap = false,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowHeight || textLayoutResult.didOverflowWidth) {
                if (!textSize.isUnspecified && textSize.value > 10f) {
                    textSize = (textSize.value * 0.9f).sp
                }
            }
        },
        modifier = modifier
    )
}



// -------------------------- Preview --------------------------


@Preview(showBackground = true, name = "Active Player")
@Composable
private fun ActivePlayerPreview() {
    MaterialTheme {
        PlayerClock(
            player = Player.ONE,
            formattedTime = "5:00",
            moveCount = 5,
            isActive = true,
            isFinished = false,
            enabled = true,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Inactive Player")
@Composable
private fun InactivePlayerPreview() {
    MaterialTheme {
        PlayerClock(
            player = Player.TWO,
            formattedTime = "4:45",
            moveCount = 5,
            isActive = false,
            isFinished = false,
            enabled = true,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Finished Player")
@Composable
private fun FinishedPlayerPreview() {
    MaterialTheme {
        PlayerClock(
            player = Player.ONE,
            formattedTime = "0:00.0",
            moveCount = 42,
            isActive = false,
            isFinished = true,
            enabled = false,
            onClick = {}
        )
    }
}
