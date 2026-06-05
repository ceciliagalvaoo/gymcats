package com.gymcats.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object About : Screen("about")
    object Workout : Screen("workout")
    object CycleLog : Screen("cyclelog/{workoutId}") {
        fun createRoute(workoutId: Long) = "cyclelog/$workoutId"
    }
    object PhotoCapture : Screen("photo/{workoutId}") {
        fun createRoute(workoutId: Long) = "photo/$workoutId"
    }
    object Progress : Screen("progress")
    object ExerciseProgression : Screen("progression/{exerciseName}/{months}") {
        fun createRoute(name: String, months: Int) = "progression/$name/$months"
    }
    object Profile : Screen("profile")
}
