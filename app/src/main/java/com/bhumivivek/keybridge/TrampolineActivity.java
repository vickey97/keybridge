package com.bhumivivek.keybridge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Invisible trampoline. When a remote button that fires a system "notification handler"
 * intent is pressed (e.g. the TCL gear / notification button -> TOGGLE_NOTIFICATION_HANDLER_PANEL),
 * the OS launches this activity. We immediately open the TCL Picture pane
 * (android.settings.DISPLAY_SETTINGS) as an overlay over whatever is playing, then finish.
 *
 * No accessibility service is involved, so volume long-press (hold-to-ramp) keeps working.
 */
public class TrampolineActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Intent i = new Intent("android.settings.DISPLAY_SETTINGS");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Exception e) {
            // no-op
        }
        finish();
    }
}
