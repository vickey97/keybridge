# KeyBridge

Make an Android TV / Google TV remote button open whatever you want — **without an accessibility
service**, so volume long-press and other system keys keep working normally.

## How it works
After you disable the Google TV launcher, the TCL **gear / notification button** still fires the
system intent `TOGGLE_NOTIFICATION_HANDLER_PANEL`, but nothing handles it, so SystemUI logs:

```
E/TvNotificationPanel: Not launching notification handler activity:
    Could not resolve activityInfo for intent ...TOGGLE_NOTIFICATION_HANDLER_PANEL
```

(SystemUI only accepts a *system* app as the handler, so a sideloaded app can't register directly.)

KeyBridge runs a tiny foreground service that **watches logcat for that exact line** and, when it
appears, opens the **Picture pane** (`android.settings.DISPLAY_SETTINGS`) as an overlay over whatever
is playing — so HDR / Dolby Vision picture modes stay live with on-screen preview.

Because it only reads logs (never filters key events), **volume hold-to-ramp is untouched.**

## Permissions (granted once via ADB)
```
pm grant   com.bhumivivek.keybridge android.permission.READ_LOGS
appops set com.bhumivivek.keybridge SYSTEM_ALERT_WINDOW allow
```
`READ_LOGS` lets it watch the log; `SYSTEM_ALERT_WINDOW` exempts it from background-activity-launch
limits so it can open the pane. A `BootReceiver` restarts the service after reboots.

## Build (no local SDK needed)
Push to GitHub → the **Build APK** workflow compiles `app-debug.apk` (artifact `keybridge-apk`).

## Install & start
```
adb install -r app-debug.apk
adb shell am start-foreground-service -n com.bhumivivek.keybridge/.LogWatchService
```

## Roadmap
- Configurable target action (not just the Picture pane).
- Support additional buttons / log signatures.

## License
MIT
