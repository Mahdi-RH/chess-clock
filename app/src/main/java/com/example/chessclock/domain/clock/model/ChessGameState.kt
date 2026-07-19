package com.example.chessclock.domain.clock.model

data class ChessGameState(
    val timeControl: TimeControl = TimeControl.presets[1], // is it stable?
    val playerOneMillis: Long = timeControl.baseMillis,
    val playerTwoMillis: Long = timeControl.baseMillis,
    val activePlayer: Player? = null,
    val status: ClockStatus = ClockStatus.READY,
    val playerOneMoves: Int = 0,
    val playerTwoMoves: Int = 0,
) {
    fun remainingTime(player: Player): Long = when (player) {
        Player.ONE -> playerOneMillis
        Player.TWO -> playerTwoMillis
    }
}