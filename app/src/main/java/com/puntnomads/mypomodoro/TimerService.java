package com.puntnomads.mypomodoro;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;

public class TimerService extends Service {
    public static final long WORKING_TIME_MILLIS = 50*60*1000;
    public static final long SHORT_BREAK_TIME_MILLIS = 10*60*1000;
    public static final long LONG_BREAK_TIME_MILLIS = 30*60*1000;
    public boolean Working = true;
    public boolean Break = false;
    public int numberOfBreaks = 0;
    public long remaining = 0;
    public static final String WORKING_TASK = "Working Time";
    public static final String SHORT_BREAK_TASK = "Walk Outside";
    public static final String LONG_BREAK_TASK = "Nap at Table";

    private final static String TAG = "TimerService";
    boolean finished = false;
    public static final String COUNTDOWN = "com.puntnomads.mypomodoro.COUNTDOWN";
    Intent timer = new Intent(COUNTDOWN);
    CountDownTimer countDownTimer = null;
    Vibrator vibrator;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setPomodoros();
        if(finished){
            NotificationReceiver.completeWakefulIntent(intent);
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    public void setPomodoros(){
        if(Working){
            setCountDownTimer(WORKING_TIME_MILLIS, WORKING_TASK);
        } else if(Break && numberOfBreaks != 3){
            numberOfBreaks++;
            setCountDownTimer(SHORT_BREAK_TIME_MILLIS, SHORT_BREAK_TASK);
        } else {
            numberOfBreaks = 0;
            setCountDownTimer(LONG_BREAK_TIME_MILLIS, LONG_BREAK_TASK);
        }
    }

    public void setCountDownTimer(long time, final String task){
        if(remaining != 0){
            time = remaining;
        }
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DoNotDimScreen");
        wakelock.acquire(time + 1000);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        countDownTimer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timer.putExtra("countdown", millisUntilFinished);
                timer.putExtra("task", task);
                remaining = millisUntilFinished;
                sendBroadcast(timer);
            }
            @Override
            public void onFinish() {
                vibrator.vibrate(5000);
                remaining = 0;
                Working = !Working;
                Break = !Working;
                setPomodoros();
            }
        };
        countDownTimer.start();
    }

    public void countDownTimerPause() {
        countDownTimer.cancel();
    }

    public void countDownTimerReset() {
        countDownTimer.cancel();
        remaining = 0;
        Working = true;
        Break = false;
        numberOfBreaks = 0;
        timer.putExtra("countdown", WORKING_TIME_MILLIS);
        timer.putExtra("task", WORKING_TASK);
        sendBroadcast(timer);
    }

    private BroadcastReceiver pauseBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            countDownTimerPause();
        }
    };

    private BroadcastReceiver resetBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            countDownTimerReset();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(pauseBroadcastReceiver, new IntentFilter(MainActivity.PAUSE));
        registerReceiver(resetBroadcastReceiver, new IntentFilter(MainActivity.RESET));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(pauseBroadcastReceiver);
        unregisterReceiver(resetBroadcastReceiver);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
