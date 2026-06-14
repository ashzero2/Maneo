package com.maneo.app.feature.blocker.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maneo.app.feature.blocker.repository.BlockedAppsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap?,
    val isBlocked: Boolean,
)

private data class RawApp(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap?,
)

@HiltViewModel
class AppSelectorViewModel @Inject constructor(
    private val blockedAppsRepository: BlockedAppsRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _rawApps = MutableStateFlow<List<RawApp>>(emptyList())
    private val _searchQuery = MutableStateFlow("")

    val searchQuery: StateFlow<String> = _searchQuery

    val apps: StateFlow<List<InstalledApp>> = combine(
        _rawApps,
        blockedAppsRepository.blockedApps,
        _searchQuery,
    ) { raw, blocked, query ->
        raw
            .filter { query.isBlank() || it.label.contains(query, ignoreCase = true) }
            .map { InstalledApp(it.packageName, it.label, it.icon, it.packageName in blocked) }
            .sortedWith(compareByDescending<InstalledApp> { it.isBlocked }.thenBy { it.label })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _rawApps.value = loadInstalledApps()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setBlocked(packageName: String, blocked: Boolean) {
        viewModelScope.launch {
            blockedAppsRepository.setBlocked(packageName, blocked)
        }
    }

    private fun loadInstalledApps(): List<RawApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        @Suppress("DEPRECATION")
        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            pm.queryIntentActivities(intent, 0)
        }
        return resolveInfos
            .map { it.activityInfo.packageName }
            .distinct()
            .filter { it != context.packageName }
            .mapNotNull { pkg ->
                try {
                    val info = pm.getApplicationInfo(pkg, 0)
                    RawApp(
                        packageName = pkg,
                        label = pm.getApplicationLabel(info).toString(),
                        icon = pm.getApplicationIcon(pkg).toImageBitmap(),
                    )
                } catch (_: Exception) { null }
            }
            .sortedBy { it.label }
    }
}

private fun Drawable.toImageBitmap(): ImageBitmap {
    val w = intrinsicWidth.coerceAtLeast(1)
    val h = intrinsicHeight.coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, w, h)
    draw(canvas)
    return bitmap.asImageBitmap()
}
