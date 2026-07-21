package com.example.chessclock.domain.clock.model

data class ChessGameState(
    val timeControl: TimeControl,
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

    companion object {
        fun initial(timeControl: TimeControl): ChessGameState {
            return ChessGameState(
                timeControl = timeControl,
                playerOneMillis = timeControl.baseMillis,
                playerTwoMillis = timeControl.baseMillis,
            )
        }
    }
}
