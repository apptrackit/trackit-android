import androidx.compose.runtime.Composable

sealed class Screen(val route: String) {
    object Progress : Screen("progress")
    object EditMetric : Screen("edit_metric")
    object ViewMetricHistory : Screen("view_metric_history")
    // ... existing code ...
} 