package com.gymcats.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gymcats.presentation.about.AboutScreen
import com.gymcats.presentation.cyclelog.CycleLogScreen
import com.gymcats.presentation.home.HomeScreen
import com.gymcats.presentation.login.LoginScreen
import com.gymcats.presentation.onboarding.OnboardingScreen
import com.gymcats.presentation.photo.PhotoCaptureScreen
import com.gymcats.presentation.profile.ProfileScreen
import com.gymcats.presentation.progress.ExerciseProgressionScreen
import com.gymcats.presentation.progress.ProgressScreen
import com.gymcats.presentation.workout.WorkoutScreen

@Composable
fun NavGraph(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(onAuthenticated = { route ->
                navController.navigate(route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinish = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.About.route) {
            AboutScreen(navController = navController)
        }
        composable(Screen.Workout.route) {
            WorkoutScreen(
                onWorkoutClosed = { workoutId, startTimeMs ->
                    navController.navigate(Screen.CycleLog.createRoute(workoutId, startTimeMs))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.CycleLog.route,
            arguments = listOf(
                navArgument("workoutId") { type = NavType.LongType },
                navArgument("startTimeMs") { type = NavType.LongType }
            )
        ) {
            val workoutId = it.arguments?.getLong("workoutId") ?: 0L
            val startTimeMs = it.arguments?.getLong("startTimeMs") ?: System.currentTimeMillis()
            CycleLogScreen(
                workoutId = workoutId,
                startTimeMs = startTimeMs,
                onSaved = { navController.navigate(Screen.PhotoCapture.createRoute(workoutId)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.PhotoCapture.route,
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
        ) {
            val workoutId = it.arguments?.getLong("workoutId") ?: 0L
            PhotoCaptureScreen(
                workoutId = workoutId,
                onDone = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }
        composable(Screen.Progress.route) {
            ProgressScreen(navController = navController)
        }
        composable(
            route = Screen.ExerciseProgression.route,
            arguments = listOf(
                navArgument("exerciseName") { type = NavType.StringType },
                navArgument("months") { type = NavType.IntType }
            )
        ) {
            val name = android.net.Uri.decode(it.arguments?.getString("exerciseName") ?: "")
            val months = it.arguments?.getInt("months") ?: 3
            ExerciseProgressionScreen(exerciseName = name, months = months, navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
    }
}
