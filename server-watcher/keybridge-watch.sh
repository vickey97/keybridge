#!/usr/bin/env bash
# KeyBridge server watcher
# Opens the TCL Picture pane (android.settings.DISPLAY_SETTINGS) when the remote's
# gear / notification button is pressed. Runs on an always-on machine (e.g. a mini PC)
# that can reach the TV over ADB. Nothing is installed on the TV, no accessibility
# service is used -> volume hold-to-ramp stays perfect.
#
# How it works: the gear button makes SystemUI log
#   E/TvNotificationPanel: ... Could not resolve activityInfo for intent
#       android.app.action.TOGGLE_NOTIFICATION_HANDLER_PANEL
# ADB (shell uid) can read that line; on a match we fire the Picture pane back.
set -u

TV="${TV_ADDR:-192.168.178.41:5555}"
ADB="${ADB_BIN:-adb}"

open_pane() {
  "$ADB" -s "$TV" shell "am start -a android.settings.DISPLAY_SETTINGS" </dev/null >/dev/null 2>&1
}

echo "keybridge-watch: TV=$TV adb=$ADB"
while true; do
  "$ADB" connect "$TV" >/dev/null 2>&1
  # Stream only TvNotificationPanel errors; react to the gear-press line.
  "$ADB" -s "$TV" shell "logcat -v brief TvNotificationPanel:E '*:S'" 2>/dev/null \
    | while IFS= read -r line; do
        case "$line" in
          *NOTIFICATION_HANDLER_PANEL*) open_pane ;;
        esac
      done
  # logcat ended (TV reboot / network drop) -> wait and reconnect
  echo "keybridge-watch: stream ended, reconnecting in 5s..."
  sleep 5
done
