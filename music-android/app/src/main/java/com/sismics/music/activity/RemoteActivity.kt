package com.sismics.music.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import com.google.zxing.integration.android.IntentIntegrator
import com.sismics.music.R
import com.sismics.music.model.ApplicationContext
import com.sismics.music.util.RemoteControlUtil

/**
 * Remote control activity.

 * @author bgamard
 */
class RemoteActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if logged in
        if (!ApplicationContext.isLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Inflate the layout
        setContentView(R.layout.activity_remote)

        // Set up the action bar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setHomeButtonEnabled(true)

        // Action buttons
        val btnPrevious = findViewById(R.id.btnPrevious) as ImageButton
        val btnPlay = findViewById(R.id.btnPlay) as ImageButton
        val btnPause = findViewById(R.id.btnPause) as ImageButton
        val btnNext = findViewById(R.id.btnNext) as ImageButton

        btnPrevious.setOnClickListener {
            val command = RemoteControlUtil.buildCommand(RemoteControlUtil.Command.PREVIOUS)
            if (command != null) {
                RemoteControlUtil.sendCommand(this@RemoteActivity, command, 0)
            }
        }

        btnPlay.setOnClickListener {
            val command = RemoteControlUtil.buildCommand(RemoteControlUtil.Command.PLAY)
            if (command != null) {
                RemoteControlUtil.sendCommand(this@RemoteActivity, command, 0)
            }
        }

        btnPause.setOnClickListener {
            val command = RemoteControlUtil.buildCommand(RemoteControlUtil.Command.PAUSE)
            if (command != null) {
                RemoteControlUtil.sendCommand(this@RemoteActivity, command, 0)
            }
        }

        btnNext.setOnClickListener {
            val command = RemoteControlUtil.buildCommand(RemoteControlUtil.Command.NEXT)
            if (command != null) {
                RemoteControlUtil.sendCommand(this@RemoteActivity, command, 0)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        val scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (scanResult != null && scanResult.contents != null) {
            val token = scanResult.contents
            RemoteControlUtil.connect(this, token)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.remote, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            R.id.connect_player -> {
                IntentIntegrator(this).initiateScan(IntentIntegrator.QR_CODE_TYPES)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
