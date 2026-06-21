package com.bhumivivek.keybridge;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Foreground service that tails logcat for the line SystemUI prints when the gear /
 * notification button is pressed but has no system handler:
 *
 *   E/TvNotificationPanel: Not launching notification handler activity:
 *       Could not resolve activityInfo for intent ...TOGGLE_NOTIFICATION_HANDLER_PANEL
 *
 * When seen, we open the TCL Picture pane (android.settings.DISPLAY_SETTINGS) as an overlay.
 * No accessibility service -> volume long-press is never touched.
 */
public class LogWatchService extends Service {

    private volatile boolean running = false;
    private long serviceStart = 0L;
    private long lastOpen = 0L;

    @Override
    public void onCreate() {
        super.onCreate();
        serviceStart = System.currentTimeMillis();
        goForeground();
    }

    private void goForeground() {
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!running) {
            running = true;
            Thread t = new Thread(this::watchLoop);
            t.setDaemon(true);
            t.start();
        }
        return START_STICKY;
    }

    private void watchLoop() {
        while (running) {
            Process p = null;
            try {
                // -T 1 starts near "now" so we don't replay the whole history.
                p = Runtime.getRuntime().exec(new String[]{
                        "logcat", "-T", "1", "-v", "brief", "TvNotificationPanel:E", "*:S"});
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while (running && (line = r.readLine()) != null) {
                    if (line.contains("NOTIFICATION_HANDLER_PANEL")) {
                        openPicturePane();
                    }
                }
            } catch (Exception ignored) {
            } finally {
                if (p != null) {
                    p.destroy();
                }
            }
            if (running) {
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void openPicturePane() {
        long now = System.currentTimeMillis();
        if (now - serviceStart < 3000) return; // ignore the historical line at startup
        if (now - lastOpen < 700) return;      // debounce a burst of identical lines
        lastOpen = now;
        try {
            Intent i = new Intent("android.settings.DISPLAY_SETTINGS");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Exception ignored) {
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        running = false;
        super.onDestroy();
    }
}
