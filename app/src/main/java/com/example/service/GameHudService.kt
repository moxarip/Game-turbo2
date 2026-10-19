package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import com.example.service.ui.FloatingCrosshairView
import com.example.service.ui.FloatingHudPillView
import com.example.service.ui.FloatingSidePanelView
import com.example.service.ui.FloatingVisionFilterView
import com.example.telemetry.HardwareTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GameHudService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var windowManager: WindowManager
    private lateinit var audioManager: AudioManager
    private lateinit var telemetry: HardwareTelemetry

    // Overlay Views
    private var pillView: View? = null
    private var pillParams: WindowManager.LayoutParams? = null

    private var sidePanelView: View? = null
    private var sidePanelParams: WindowManager.LayoutParams? = null

    private var crosshairView: View? = null
    private var crosshairParams: WindowManager.LayoutParams? = null

    private var visionFilterView: View? = null
    private var visionFilterParams: WindowManager.LayoutParams? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        telemetry = HardwareTelemetry(applicationContext)
        telemetry.start()

        startForegroundNotification()
        GameHudManager.setServiceRunning(true)

        if (Settings.canDrawOverlays(this)) {
            initPillOverlay()
            initSidePanelOverlay()
            initCrosshairOverlay()
            initVisionFilterOverlay()
        }

        observeHudState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_HUD -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_SIDEBAR -> {
                GameHudManager.toggleSidebar()
            }
        }
        return START_STICKY
    }

    private fun startForegroundNotification() {
        val channelId = "gamecore_hud_service"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.hud_service_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.hud_service_channel_desc)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, GameHudService::class.java).apply {
            action = ACTION_STOP_HUD
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.hud_notification_title))
            .setContentText(getString(R.string.hud_notification_text))
            .setContentIntent(openAppPendingIntent)
            .addAction(0, getString(R.string.hud_notification_stop), stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun initPillOverlay() {
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 300
        }
        pillParams = params

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@GameHudService)
            setViewTreeSavedStateRegistryOwner(this@GameHudService)
            setContent {
                FloatingHudPillView(
                    telemetryState = telemetry.telemetry,
                    hudConfig = GameHudManager.hudConfig,
                    onPillClick = { GameHudManager.toggleSidebar() }
                )
            }
        }

        // Add drag listener with edge snap
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isMoving = false

        composeView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isMoving = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()
                    if (Math.hypot(deltaX.toDouble(), deltaY.toDouble()) > 10) {
                        isMoving = true
                        params.x = initialX + deltaX
                        params.y = initialY + deltaY
                        try {
                            windowManager.updateViewLayout(composeView, params)
                        } catch (_: Exception) {}
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isMoving) {
                        GameHudManager.toggleSidebar()
                    }
                    true
                }
                else -> false
            }
        }

        pillView = composeView
        try {
            windowManager.addView(composeView, params)
        } catch (_: Exception) {}
    }

    private fun initSidePanelOverlay() {
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }
        sidePanelParams = params

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@GameHudService)
            setViewTreeSavedStateRegistryOwner(this@GameHudService)
            setContent {
                FloatingSidePanelView(
                    telemetryState = telemetry.telemetry,
                    hudConfig = GameHudManager.hudConfig,
                    visionConfig = GameHudManager.visionConfig,
                    crosshairConfig = GameHudManager.crosshairConfig,
                    onClose = { GameHudManager.setSidebarOpen(false) },
                    onResetVision = { GameHudManager.resetVisionFilter() },
                    audioManager = audioManager
                )
            }
        }
        sidePanelView = composeView
    }

    private fun initCrosshairOverlay() {
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            x = 0
            y = 0
        }
        crosshairParams = params

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@GameHudService)
            setViewTreeSavedStateRegistryOwner(this@GameHudService)
            setContent {
                FloatingCrosshairView(config = GameHudManager.crosshairConfig)
            }
        }
        crosshairView = composeView
    }

    private fun initVisionFilterOverlay() {
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }
        visionFilterParams = params

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@GameHudService)
            setViewTreeSavedStateRegistryOwner(this@GameHudService)
            setContent {
                FloatingVisionFilterView(config = GameHudManager.visionConfig)
            }
        }
        visionFilterView = composeView
    }

    private fun observeHudState() {
        // Observe Sidebar Open/Close
        serviceScope.launch {
            GameHudManager.isSidebarOpen.collectLatest { isOpen ->
                if (isOpen) {
                    if (sidePanelView?.parent == null && sidePanelParams != null) {
                        try {
                            windowManager.addView(sidePanelView, sidePanelParams)
                        } catch (_: Exception) {}
                    }
                } else {
                    if (sidePanelView?.parent != null) {
                        try {
                            windowManager.removeView(sidePanelView)
                        } catch (_: Exception) {}
                    }
                }
            }
        }

        // Observe Crosshair config
        serviceScope.launch {
            GameHudManager.crosshairConfig.collectLatest { config ->
                if (config.isEnabled) {
                    crosshairParams?.let { p ->
                        p.x = config.offsetX
                        p.y = config.offsetY
                        if (crosshairView?.parent == null) {
                            try {
                                windowManager.addView(crosshairView, p)
                            } catch (_: Exception) {}
                        } else {
                            try {
                                windowManager.updateViewLayout(crosshairView, p)
                            } catch (_: Exception) {}
                        }
                    }
                } else {
                    if (crosshairView?.parent != null) {
                        try {
                            windowManager.removeView(crosshairView)
                        } catch (_: Exception) {}
                    }
                }
            }
        }

        // Observe Hunt Vision filter
        serviceScope.launch {
            GameHudManager.visionConfig.collectLatest { config ->
                if (config.isEnabled) {
                    if (visionFilterView?.parent == null && visionFilterParams != null) {
                        try {
                            windowManager.addView(visionFilterView, visionFilterParams)
                        } catch (_: Exception) {}
                    }
                } else {
                    if (visionFilterView?.parent != null) {
                        try {
                            windowManager.removeView(visionFilterView)
                        } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        serviceScope.cancel()
        telemetry.stop()
        GameHudManager.setServiceRunning(false)

        try {
            pillView?.let { if (it.parent != null) windowManager.removeView(it) }
            sidePanelView?.let { if (it.parent != null) windowManager.removeView(it) }
            crosshairView?.let { if (it.parent != null) windowManager.removeView(it) }
            visionFilterView?.let { if (it.parent != null) windowManager.removeView(it) }
        } catch (_: Exception) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIFICATION_ID = 8881
        const val ACTION_STOP_HUD = "com.example.action.STOP_HUD"
        const val ACTION_TOGGLE_SIDEBAR = "com.example.action.TOGGLE_SIDEBAR"
    }
}
