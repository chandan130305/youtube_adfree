package com.example.myapp

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.Toast
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

class MainActivity : Activity() {
    private lateinit var session: GeckoSession
    private var currentUrl = "https://m.youtube.com"
    private lateinit var btnDownload: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val geckoView = findViewById<GeckoView>(R.id.geckoView)
        btnDownload = findViewById<Button>(R.id.btnDownload)

        val runtime = GeckoRuntime.create(this)
        session = GeckoSession()
        session.open(runtime)
        geckoView.setSession(session)

        // Install our extensions
        runtime.webExtensionController.install("resource://android/assets/ublock.xpi")
        runtime.webExtensionController.install("resource://android/assets/cleaner.xpi")

        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean
            ) {
                if (url != null) {
                    currentUrl = url
                }
            }
        }

        // FULLSCREEN HANDLER
        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onFullScreen(session: GeckoSession, fullScreen: Boolean) {
                if (fullScreen) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    @Suppress("DEPRECATION")
                    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                    btnDownload.visibility = View.GONE
                } else {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    @Suppress("DEPRECATION")
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                    btnDownload.visibility = View.VISIBLE
                }
            }
        }

        // Show a popup so you know the delay is happening intentionally
        Toast.makeText(this, "Loading Adblocker...", Toast.LENGTH_LONG).show()

        // Wait exactly 3 seconds before opening YouTube to ensure uBlock is fully active
        Handler(Looper.getMainLooper()).postDelayed({
            session.loadUri("https://m.youtube.com")
        }, 3000)

        btnDownload.setOnClickListener {
            if (currentUrl.contains("watch") || currentUrl.contains("youtu.be")) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("YouTube URL", currentUrl)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Link copied! Paste it in the box to download.", Toast.LENGTH_LONG).show()
                session.loadUri("https://vidssave.com/youtube-video-downloader-8hs")
            } else {
                Toast.makeText(this, "Please open a specific video first!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        session.goBack()
    }
}
