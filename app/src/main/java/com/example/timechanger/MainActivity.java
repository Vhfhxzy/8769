package com.example.timechanger;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    
    private TextView statusText;
    private TextView currentTimeText;
    private Button toggleButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        statusText = findViewById(R.id.status_text);
        currentTimeText = findViewById(R.id.current_time_text);
        toggleButton = findViewById(R.id.toggle_button);
        
        updateUI();
        
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean currentlyEnabled = TimeController.isTimeModificationEnabled(MainActivity.this);
                if (currentlyEnabled) {
                    TimeController.disableTimeModification(MainActivity.this);
                    Toast.makeText(MainActivity.this, "Time modification disabled", Toast.LENGTH_SHORT).show();
                } else {
                    TimeController.enableTimeModification(MainActivity.this);
                    Toast.makeText(MainActivity.this, "Time set to 2025-05-16", Toast.LENGTH_SHORT).show();
                }
                updateUI();
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }
    
    private void updateUI() {
        boolean enabled = TimeController.isTimeModificationEnabled(this);
        
        if (enabled) {
            statusText.setText("Status: Enabled (2025-05-16)");
            toggleButton.setText("Disable Time Modification");
        } else {
            statusText.setText("Status: Disabled (Using Real Time)");
            toggleButton.setText("Enable Time Modification");
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        currentTimeText.setText("Current Time: " + sdf.format(new Date()));
    }
} 