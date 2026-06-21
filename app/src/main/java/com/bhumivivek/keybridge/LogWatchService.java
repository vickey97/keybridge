package com.bhumivivek.keybridge;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Foreground service that tails logcat for the line SystemUI prints when the gear /
 * notification button is pressed with no system handler, then opens the Picture pane.
 * No accessibility service -> volume long-press is never touched.
 *
 * v3: verbose logging under tag "KeyBridge" + reads the full log (no tag filter) so we can
 * see exactly where things break.
 */
public class LogWatchService extends Service {

    private static final String TAG = "KeyBridge";

    private volatile boolean running = false;
    private long serviceStart = 0L;
    private long lastOpen = 0L;

    @Override
    public void onCreate() {
        super.onCreate();
        serviceStart = System.currentTimeMillis();
        Log.i(TAG, "onCreate");
        goForeground();
    }

    private void goForeground() {
        try {
            String chId = "keybridge";
            Notification.Builder b;
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationManager nm = getSystemService(NotificationManager.class);
                if (nm != null) {
                    nm.createNotificationChannel(
                            new NotificationChannel(chId, "KeyBridge", NotificationManager.IMPORTANCE_MIN));
                }
                b = new Notification.Builder(this, chId);
            } else {
                b = new Notification.Builder(this);
            }
            Notification n = b
                    .setContentTitle("KeyBridge")
                    .setContentText("Gear button -> Picture pane")
                    .setSmallIcon(android.R.drawable.ic_menu_preferences)
                    .build();
            startForeground(1, n);
            Log.i(TAG, "goForeground ok");
        } catch (Exception e) {
            Log.e(TAG, "goForeground failed", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand running=" + running);
        if (!running) {
            running = true;
            Thread t = new Thread(this::watchLoop);
            t.setDaemon(true);
            t.start();
        }
        return START_STICKY;
    }

    private void watchLoop() {
        Log.i(TAG, "watchLoop begin");
        while (running) {
            Process p = null;
            try {
                Log.i(TAG, "exec logcat");
                p = Runtime.getRuntime().exec(new String[]{"logcat", "-T", "1", "-v", "brief"});
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                Log.i(TAG, "reader started");
                boolean firstLogged = false;
                String line;
                while (running && (line = r.readLine()) != null) {
                    if (!firstLogged) {
                        Log.i(TAG, "first line read ok");
                        firstLogged = true;
                    }
                    if (line.contains("NOTIFICATION_HANDLER_PANEL")) {
                        Log.i(TAG, "MATCH: " + line);
                        openPicturePane();
                    }
                }
                Log.w(TAG, "logcat stream ended");
            } catch (Exception e) {
                Log.e(TAG, "logcat read error", e);
            } finally {
                if (p != null) {
                    p.destroy();
                }
            }
            if (running) {
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
            }
        }
        Log.i(TAG, "watchLoop end");
    }

    private void openPicturePane() {
        long now = System.currentTimeMillis();
        if (now - serviceStart < 3000) { Log.i(TAG, "skip (startup grace)"); return; }
        if (now - lastOpen < 700) { Log.i(TAG, "skip (debounce)"); return; }
        lastOpen = now;
        try {
            Intent i = new Intent("android.settings.DISPLAY_SETTINGS");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            Log.i(TAG, "startActivity DISPLAY_SETTINGS issued");
        } catch (Exception e) {
            Log.e(TAG, "startActivity failed", e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        running = false;
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }
}
