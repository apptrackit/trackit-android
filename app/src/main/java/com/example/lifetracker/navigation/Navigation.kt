import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.lifetracker.ui.screens.health.ProgressScreen
import com.example.lifetracker.ui.screens.health.ViewMetricHistoryScreen
import com.example.lifetracker.ui.viewmodel.HealthViewModel
import com.example.lifetracker.ui.viewmodel.PhotoViewModel

@Composable
fun Navigation(
    navController: NavHostController,
    healthViewModel: HealthViewModel,
    photoViewModel: PhotoViewModel
) {
    NavHost(navController = navController, startDestination = Screen.Progress.route) {
        composable(Screen.Progress.route) {
            ProgressScreen(
                onNavigateToEditMetric = { metricName ->
                    navController.navigate("${Screen.EditMetric.route}/$metricName")
                },
                onNavigateToViewMetricHistory = { metricName, unit ->
                    navController.navigate("${Screen.ViewMetricHistory.route}/$metricName/$unit")
                },
                viewModel = healthViewModel,
                navController = navController
            )
        }
        
        composable(
            route = "${Screen.ViewMetricHistory.route}/{metricName}/{unit}",
            arguments = listOf(
                navArgument("metricName") { type = NavType.StringType },
                navArgument("unit") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val metricName = backStackEntry.arguments?.getString("metricName") ?: ""
            val unit = backStackEntry.arguments?.getString("unit") ?: ""
            ViewMetricHistoryScreen(
                navController = navController,
                viewModel = healthViewModel,
                metricName = metricName,
                unit = unit
            )
        }
    }
} 