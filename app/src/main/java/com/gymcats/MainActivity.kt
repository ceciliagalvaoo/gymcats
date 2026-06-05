package com.gymcats

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.gymcats.data.repository.AccountRepository
import com.gymcats.data.repository.UserRepository
import com.gymcats.presentation.navigation.NavGraph
import com.gymcats.presentation.navigation.Screen
import com.gymcats.ui.theme.GymCatsTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var accountRepository: AccountRepository
    @Inject lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }

        setContent {
            GymCatsTheme {
                val navController = rememberNavController()
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    startDestination = if (accountRepository.isAuthenticated()) {
                        if (userRepository.hasProfile()) Screen.Home.route else Screen.Onboarding.route
                    } else {
                        Screen.Login.route
                    }
                }

                startDestination?.let {
                    NavGraph(navController = navController, startDestination = it)
                }
            }
        }
    }
}
