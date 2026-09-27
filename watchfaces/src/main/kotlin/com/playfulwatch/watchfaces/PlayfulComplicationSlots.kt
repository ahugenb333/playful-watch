package com.playfulwatch.watchfaces

object PlayfulComplicationSlots {
    const val WEATHER = 1
    const val STEPS = 2
    const val HEART_RATE = 3

    val vitalsTriad: Set<Int> = setOf(WEATHER, STEPS, HEART_RATE)
}
