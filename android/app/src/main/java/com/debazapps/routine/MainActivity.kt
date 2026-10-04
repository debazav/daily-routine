package com.debazapps.routine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.debazapps.routine.ui.day.DayScreen
import com.debazapps.routine.ui.day.DayViewModel
import com.debazapps.routine.ui.theme.RoutineTheme
import com.debazapps.routine.ui.week.WeekScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoutineTheme {
                RoutineNavHost()
            }
        }
    }
}

@Composable
private fun RoutineNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "week") {
        composable("week") {
            WeekScreen(onDayClick = { day -> navController.navigate("day/$day") })
        }
        composable(
            route = "day/{${DayViewModel.DAY_ARG}}",
            arguments = listOf(navArgument(DayViewModel.DAY_ARG) { type = NavType.IntType }),
        ) {
            DayScreen(onBack = { navController.popBackStack() })
        }
    }
}
