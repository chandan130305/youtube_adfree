package com.example.myapp

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import android.content.ActivityNotFoundException
import android.widget.Toast

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // This is now set to YouTube
        val url = "https://m.youtube.com" 
        
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        
        customTabsIntent.intent.setPackage("com.brave.browser")
        
        try {
            customTabsIntent.launchUrl(this, Uri.parse(url))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Brave Browser is not installed", Toast.LENGTH_LONG).show()
        }
        
        finish() 
    }
}