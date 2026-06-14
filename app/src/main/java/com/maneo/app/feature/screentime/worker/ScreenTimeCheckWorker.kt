package com.maneo.app.feature.screentime.worker

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.maneo.app.core.util.NotificationHelper
import com.maneo.app.feature.blocker.repository.BlockedAppsRepository
import com.maneo.app.feature.screentime.repository.ScreenTimeRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

@HiltWorker
class ScreenTimeCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val blockedAppsRepository: BlockedAppsRepository,
    private val screenTimeRepository: ScreenTimeRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!hasUsageStatsPermission()) return Result.success()

        val threshold = screenTimeRepository.getThresholdMins()
        val blockedApps = blockedAppsRepository.blockedApps.first()
        if (blockedApps.isEmpty()) return Result.success()

        val today = LocalDate.now()
        val usageMap = queryTodayUsage()

        for (pkg in blockedApps) {
            val usedMins = (usageMap[pkg] ?: 0L) / 60_000L
            if (usedMins >= threshold && !screenTimeRepository.isNotifiedToday(pkg, today)) {
                sendWarning(pkg, usedMins.toInt())
                screenTimeRepository.markNotifiedToday(pkg, today)
            }
        }

        return Result.success()
    }

    private fun queryTodayUsage(): Map<String, Long> {
        val usm = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val start = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = System.currentTimeMillis()
        return usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
            ?.associate { it.packageName to it.totalTimeInForeground }
            ?: emptyMap()
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = applicationContext.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            applicationContext.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun sendWarning(packageName: String, usedMins: Int) {
        val appName = getAppName(packageName)
        NotificationHelper.sendScreenTimeWarning(
            context = applicationContext,
            title = "You've spent $usedMins minutes on $appName",
            body = "Maybe a moment with God before you continue?",
            packageName = packageName,
        )
    }

    @Suppress("DEPRECATION")
    private fun getAppName(packageName: String): String = try {
        val pm = applicationContext.packageManager
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0L))
        } else {
            pm.getApplicationInfo(packageName, 0)
        }
        pm.getApplicationLabel(info).toString()
    } catch (_: Exception) {
        packageName
    }
}
