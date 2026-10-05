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

        // 1. Initialize the Firefox Engine
        val runtime = GeckoRuntime.create(this)
        session = GeckoSession()
        session.open(runtime)
        geckoView.setSession(session)

        // 2. Install uBlock Origin silently in the background
        runtime.webExtensionController.install("resource://android/assets/ublock.xpi")

        // 3. Track the current URL so we know which video to download
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(session: GeckoSession, url: String?) {
                if (url != null) {
                    currentUrl = url
                }
            }
        }

        // Load YouTube initially
        session.loadUri("https://m.youtube.com")

        // 4. Setup the Download Button
        btnDownload.setOnClickListener {
            if (currentUrl.contains("watch") || currentUrl.contains("youtu.be")) {
                
                // Copy the video link to the phone's clipboard
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("YouTube URL", currentUrl)
                clipboard.setPrimaryClip(clip)
                
                Toast.makeText(this, "Link copied! Paste it in the box to download.", Toast.LENGTH_LONG).show()
                
                // Navigate instantly to your chosen downloader site
                session.loadUri("https://vidssave.com/youtube-video-downloader-8hs")
                
            } else {
                Toast.makeText(this, "Please open a specific video first!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Ensure the Android Back Button goes back in web history instead of closing the app
    override fun onBackPressed() {
        session.goBack()
    }
}