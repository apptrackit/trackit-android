package com.example.lifetracker.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import java.util.concurrent.TimeUnit
import java.util.Calendar

class StepCountRepository(private val context: Context) {
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .build()

    fun hasPermissions(): Boolean {
        return GoogleSignIn.hasPermissions(getGoogleAccount(), fitnessOptions)
    }

    fun requestPermissions(activity: Activity, requestCode: Int) {
        GoogleSignIn.requestPermissions(
            activity,
            requestCode,
            getGoogleAccount(),
            fitnessOptions
        )
    }

    fun getGoogleAccount() = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

    fun getTodayStepCount(callback: (Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build()

        Fitness.getHistoryClient(context, getGoogleAccount())
            .readData(readRequest)
            .addOnSuccessListener { response ->
                var totalSteps = 0
                response.buckets.forEach { bucket ->
                    bucket.dataSets.forEach { dataSet ->
                        dataSet.dataPoints.forEach { dataPoint ->
                            dataPoint.dataType.fields.forEach { field ->
                                totalSteps += dataPoint.getValue(field).asInt()
                            }
                        }
                    }
                }
                callback(totalSteps)
            }
            .addOnFailureListener { e ->
                Log.e("StepCountRepository", "Error getting step count", e)
                callback(0)
            }
    }

    fun getWeeklyStepCounts(callback: (List<Pair<Long, Int>>) -> Unit) {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startTime = calendar.timeInMillis

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build()

        Fitness.getHistoryClient(context, getGoogleAccount())
            .readData(readRequest)
            .addOnSuccessListener { response ->
                val stepCounts = mutableListOf<Pair<Long, Int>>()
                response.buckets.forEach { bucket ->
                    var dailySteps = 0
                    bucket.dataSets.forEach { dataSet ->
                        dataSet.dataPoints.forEach { dataPoint ->
                            dataPoint.dataType.fields.forEach { field ->
                                dailySteps += dataPoint.getValue(field).asInt()
                            }
                        }
                    }
                    stepCounts.add(Pair(bucket.getStartTime(TimeUnit.MILLISECONDS), dailySteps))
                }
                callback(stepCounts)
            }
            .addOnFailureListener { e ->
                Log.e("StepCountRepository", "Error getting weekly step counts", e)
                callback(emptyList())
            }
    }

    companion object {
        const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1001
    }
}
