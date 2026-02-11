package com.stopwatch.app.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object PlanList : Screen("plan_list")
    data object PlanEdit : Screen("plan_edit/{planId}") {
        fun createRoute(planId: Long = -1L) = "plan_edit/$planId"
    }
    data object ActiveTimer : Screen("active_timer/{planId}") {
        fun createRoute(planId: Long) = "active_timer/$planId"
    }
    data object WorkoutComplete : Screen("workout_complete/{planId}/{durationSeconds}") {
        fun createRoute(planId: Long, durationSeconds: Int) =
            "workout_complete/$planId/$durationSeconds"
    }
    data object History : Screen("history")
    data object Settings : Screen("settings")
    data object EmailOnboarding : Screen("email_onboarding")
}
