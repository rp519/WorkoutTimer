package com.stopwatch.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.stopwatch.app.screen.activetimer.ActiveTimerScreen
import com.stopwatch.app.screen.history.HistoryScreen
import com.stopwatch.app.screen.planedit.PlanEditScreen
import com.stopwatch.app.screen.planlist.PlanListScreen
import com.stopwatch.app.screen.splash.SplashScreen
import com.stopwatch.app.screen.workoutcomplete.WorkoutCompleteScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(Screen.PlanList.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.PlanList.route) {
            PlanListScreen(
                onCreatePlan = {
                    navController.navigate(Screen.PlanEdit.createRoute())
                },
                onEditPlan = { planId ->
                    navController.navigate(Screen.PlanEdit.createRoute(planId))
                },
                onStartPlan = { planId ->
                    navController.navigate(Screen.ActiveTimer.createRoute(planId))
                },
                onOpenHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }

        composable(
            route = Screen.PlanEdit.route,
            arguments = listOf(navArgument("planId") { type = NavType.LongType })
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getLong("planId") ?: -1L
            PlanEditScreen(
                planId = planId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ActiveTimer.route,
            arguments = listOf(navArgument("planId") { type = NavType.LongType })
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getLong("planId") ?: return@composable
            ActiveTimerScreen(
                planId = planId,
                onFinished = { durationSeconds ->
                    navController.navigate(
                        Screen.WorkoutComplete.createRoute(planId, durationSeconds)
                    ) {
                        popUpTo(Screen.PlanList.route)
                    }
                },
                onAbandon = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.WorkoutComplete.route,
            arguments = listOf(
                navArgument("planId") { type = NavType.LongType },
                navArgument("durationSeconds") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getLong("planId") ?: 0L
            val durationSeconds = backStackEntry.arguments?.getInt("durationSeconds") ?: 0
            WorkoutCompleteScreen(
                planId = planId,
                durationSeconds = durationSeconds,
                onDone = {
                    navController.navigate(Screen.PlanList.route) {
                        popUpTo(Screen.PlanList.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
