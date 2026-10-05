package com.example.myapp

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

class MainActivity : Activity() {
    private lateinit var session: GeckoSession
    private var currentUrl = "https://m.youtube.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val geckoView = findViewById<GeckoView>(R.id.geckoView)
        val btnDownload = findViewById<Button>(R.id.btnDownload)

        val runtime = GeckoRuntime.create(this)
        session = GeckoSession()
        session.open(runtime)
        geckoView.setSession(session)

        runtime.webExtensionController.install("resource://android/assets/ublock.xpi")

        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            // Updated signature to match the modern GeckoView requirements
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

        session.loadUri("https://m.youtube.com")

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
