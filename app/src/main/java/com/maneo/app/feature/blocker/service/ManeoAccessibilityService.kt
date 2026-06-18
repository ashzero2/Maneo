package com.maneo.app.feature.blocker.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import com.maneo.app.core.util.NotificationHelper
import com.maneo.app.feature.blocker.repository.BlockedAppsRepository
import com.maneo.app.feature.blocker.ui.InterceptActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ManeoAccessibilityService : AccessibilityService() {

    @Inject lateinit var blockedAppsRepository: BlockedAppsRepository

    @Volatile private var blockedPackages: Set<String> = emptySet()

    // After the user taps Continue anyway, hold the package name so the service
    // doesn't re-intercept during the same session. Cleared when the user
    // genuinely leaves to a different non-system app.
    @Volatile private var interceptedPackage: String? = null

    // Cache system-app lookups to avoid repeated PackageManager calls on every event.
    private val systemPackageCache = mutableMapOf<String, Boolean>()

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onServiceConnected() {
        NotificationHelper.createServiceChannel(this)
        val notification = NotificationHelper.buildKeepAliveNotification(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NotificationHelper.SERVICE_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            @Suppress("DEPRECATION")
            startForeground(NotificationHelper.SERVICE_NOTIFICATION_ID, notification)
        }
        serviceScope.launch {
            blockedAppsRepository.blockedApps.collect { packages ->
                blockedPackages = packages
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return  // ignore our own activity

        // System UI, keyboard, and other system apps fire window events during
        // in-app navigation and back-gesture animations. Letting them clear the
        // exemption causes false re-intercepts (e.g. scrolling Reels → back).
        if (isSystemPackage(pkg)) return

        // A real user-facing app came to the foreground — clear the exemption.
        if (pkg != interceptedPackage) interceptedPackage = null

        if (pkg in blockedPackages && interceptedPackage == null) {
            interceptedPackage = pkg
            launchInterceptActivity()
        }
    }

    private fun isSystemPackage(pkg: String): Boolean =
        systemPackageCache.getOrPut(pkg) {
            try {
                val flags = packageManager.getApplicationInfo(pkg, 0).flags
                (flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
            } catch (_: Exception) {
                false
            }
        }

    private fun launchInterceptActivity() {
        startActivity(
            Intent(this, InterceptActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            }
        )
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
