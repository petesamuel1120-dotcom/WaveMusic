package com.maxrave.domain.manager

/** Shared defaults and validation for the persisted glass intensity preference. */
object LiquidGlassIntensity {
    const val Default = 0.25f
    const val ReducedEffects = 0.25f

    fun normalize(value: Float): Float =
        if (value.isFinite()) value.coerceIn(0f, 1f) else Default
}
