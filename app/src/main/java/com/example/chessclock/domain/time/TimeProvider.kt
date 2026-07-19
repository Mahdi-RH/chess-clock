package com.example.chessclock.domain.time

interface TimeProvider {
    fun getElapsedRealtime(): Long
}
