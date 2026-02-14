package com.stopwatch.app.data.model

/**
 * Defines the three workout creation modes:
 * - SIMPLE: Just rounds/exercises count (no exercise selection) - original timer functionality
 * - LIBRARY: Select exercises from the exercise library with images
 * - CUSTOM: Mix of library exercises and custom user-defined exercises
 */
enum class WorkoutMode {
    /**
     * Simple timer mode - user sets rounds, exercises per round, work/rest times
     * No exercise selection required - user manages exercises themselves
     * Display shows: "Round X/Y - Exercise A/B" with timer only
     */
    SIMPLE,

    /**
     * Library mode - user selects exercises from the exercise library
     * Shows exercise names and images during workout
     * "Next Up" preview feature applies
     */
    LIBRARY,

    /**
     * Custom mode - mix library exercises with custom timed intervals
     * Can add custom exercises with name + duration + optional image
     * Full flexibility to build any workout combination
     */
    CUSTOM
}
