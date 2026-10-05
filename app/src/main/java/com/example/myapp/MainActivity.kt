package com.example.myapp

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.webkit.ValueCallback
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

        // Track URLs and auto-inject ad-removal script on every page load
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean
            ) {
                if (url != null) {
                    currentUrl = url
                    // Inject powerful ad-killing JavaScript on every page navigation
                    injectAdBlocker(session)
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

        // Load YouTube immediately now that content blocking is native
        session.loadUri("https://m.youtube.com")

        // Download Button Logic
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

    private fun injectAdBlocker(session: GeckoSession) {
        val adBlockScript = """
            (function() {
                // Remove banners, promotions, and UI clutter
                const hideElements = () => {
                    const selectors = [
                        'ytm-promoted-sparkles-web-renderer',
                        'ytm-player-legacy-desktop-watch-ads-renderer',
                        '.video-ads',
                        '.ytp-ad-module',
                        'ytm-promoted-app-install-action',
                        'a[href*="vnd.youtube"]'
                    ];
                    selectors.forEach(selector => {
                        document.querySelectorAll(selector).forEach(el => el.remove());
                    });
                };

                // Aggressively skip video ads when they appear
                const skipAds = () => {
                    const video = document.querySelector('video');
                    const skipBtn = document.querySelector('.ytp-ad-skip-button, .videoAdUiSkipButton, ytm-ad-skip-button-renderer');
                    
                    if (skipBtn) {
                        skipBtn.click();
                    }
                    
                    if (video && document.querySelector('.ad-showing, .ad-interrupting')) {
                        video.currentTime = video.duration || video.currentTime + 10;
                    }
                };

                setInterval(() => {
                    hideElements();
                    skipAds();
                }, 300);
            })();
        """.trimIndent()

        session.loadUri("javascript:$adBlockScript")
    }

    override fun onBackPressed() {
        session.goBack()
    }
}
