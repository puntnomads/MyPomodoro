package com.puntnomads.mypomodoro;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class MainActivity extends Activity {

    private TextView taskTextView;
    private TextView timeTextView;
    private Button startPauseButton;
    private Button resetButton;
    private boolean playing = false;
    public static final String PAUSE = "com.puntnomads.mypomodoro.PAUSE";
    Intent pause = new Intent(PAUSE);
    public static final String RESET = "com.puntnomads.mypomodoro.RESET";
    Intent reset = new Intent(RESET);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        taskTextView = (TextView) findViewById(R.id.taskTextView);
        timeTextView = (TextView) findViewById(R.id.timeTextView);
        startPauseButton = (Button) findViewById(R.id.startPauseButton);
        resetButton = (Button) findViewById(R.id.resetButton);
        startPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playing == false) {
                    startTimer();
                    playing = true;
                    startPauseButton.setText("Pause");
                } else {
                    sendBroadcast(pause);
                    playing = false;
                    startPauseButton.setText("Start");
                }
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendBroadcast(reset);
            }
        });
    }

    private void startTimer(){
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        sendBroadcast(notificationIntent);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateGUI(intent);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(TimerService.COUNTDOWN));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onStop() {
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            // Receiver was probably already stopped in onPause()
        }
        super.onStop();
    }

    private void updateGUI(Intent intent) {
        if (intent.getExtras() != null) {
            String task = intent.getStringExtra("task");
            long millisUntilFinished = intent.getLongExtra("countdown", 0);
            SimpleDateFormat df = new SimpleDateFormat("mm:ss");
            String time = df.format(millisUntilFinished);
            taskTextView.setText(task);
            timeTextView.setText(time);
        }
    }
}
