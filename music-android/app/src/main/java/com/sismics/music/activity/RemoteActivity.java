package com.sismics.music.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.sismics.music.R;
import com.sismics.music.model.ApplicationContext;
import com.sismics.music.util.RemoteControlUtil;

/**
 * Remote control activity.
 *
 * @author bgamard
 */
public class RemoteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if logged in
        if (!ApplicationContext.getInstance().isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Inflate the layout
        setContentView(R.layout.activity_remote);

        // Set up the action bar
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // Action buttons
        ImageButton btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
        ImageButton btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        ImageButton btnPause = (ImageButton) findViewById(R.id.btnPause);
        ImageButton btnNext = (ImageButton) findViewById(R.id.btnNext);

        btnPrevious.setOnClickListener(v -> {
            String command = RemoteControlUtil.buildCommand(RemoteControlUtil.Command.PREVIOUS);
            RemoteControlUtil.sendCommand(RemoteActivity.this, command, 0);
        });

        btnPlay.setOnClickListener(v -> {
            String command = RemoteControlUtil.buildCommand(RemoteControlUtil.Command.PLAY);
            RemoteControlUtil.sendCommand(RemoteActivity.this, command, 0);
        });

        btnPause.setOnClickListener(v -> {
            String command = RemoteControlUtil.buildCommand(RemoteControlUtil.Command.PAUSE);
            RemoteControlUtil.sendCommand(RemoteActivity.this, command, 0);
        });

        btnNext.setOnClickListener(v -> {
            String command = RemoteControlUtil.buildCommand(RemoteControlUtil.Command.NEXT);
            RemoteControlUtil.sendCommand(RemoteActivity.this, command, 0);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null && scanResult.getContents() != null) {
            String token = scanResult.getContents();
            RemoteControlUtil.connect(this, token);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.remote, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.connect_player:
                new IntentIntegrator(this).initiateScan(IntentIntegrator.QR_CODE_TYPES);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
