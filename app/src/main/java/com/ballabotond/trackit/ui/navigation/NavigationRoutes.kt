package com.ballabotond.trackit.ui.navigation

// Auth routes
const val LOGIN_ROUTE = "login"
const val REGISTER_ROUTE = "register"

// Main navigation routes
const val DASHBOARD_ROUTE = "dashboard"
const val NUTRITION_ROUTE = "nutrition"
const val PHOTOS_ROUTE = "photos"
const val PROGRESS_ROUTE = "progress"
const val PROFILE_ROUTE = "profile"
const val SETTINGS_ROUTE = "settings"

// Other routes
const val EDIT_WEIGHT_ROUTE = "edit_weight"
const val EDIT_HEIGHT_ROUTE = "edit_height"
const val EDIT_BODY_FAT_ROUTE = "edit_body_fat"
const val EDIT_METRIC_ROUTE = "edit_metric/{metricName}/{unit}/{title}"
const val ADD_METRIC_DATA_ROUTE = "add_metric_data/{metricName}/{unit}/{title}"
const val VIEW_BMI_HISTORY_ROUTE = "view_bmi_history"
const val VIEW_CALCULATED_HISTORY_ROUTE = "view_calculated_history/{metricName}/{unit}/{title}"
const val EDIT_METRIC_DATA_ROUTE = "edit_metric_data/{metricName}/{unit}/{title}/{value}/{date}"
const val ADD_ENTRY_ROUTE = "add_entry"
const val PHOTO_DETAIL_ROUTE = "photo_detail/{uri}"
const val PHOTO_COMPARE_ROUTE = "photo_compare/{mainUri}/{compareUri}"
const val PHOTO_CATEGORY_ROUTE = "photo_category/{uri}"