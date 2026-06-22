# KeyBridge — server watcher (volume-safe, no app on the TV)

Runs on an always-on Linux box (mini PC / home server) that shares the network with the TV.
It watches the TV's log over ADB and opens the Picture pane when the gear/notification button
is pressed. **No app or accessibility service on the TV, so volume hold-to-ramp is untouched.**

## One-time setup
1. Install ADB on the server:
   ```
   sudo apt update && sudo apt install -y adb
   ```
2. Make sure network ADB is enabled on the TV (Developer options → USB/Network debugging).
   The TV here is at `192.168.178.41:5555`.
3. Authorize this server's ADB key on the TV (first connect shows an "Allow USB debugging?"
   prompt on the TV — accept "Always allow"):
   ```
   adb connect 192.168.178.41:5555
   adb devices        # should show "device", not "unauthorized"
   ```

## Install the service
```
sudo mkdir -p /opt/keybridge
sudo cp keybridge-watch.sh /opt/keybridge/
sudo chmod +x /opt/keybridge/keybridge-watch.sh
sudo cp keybridge-watch.service /etc/systemd/system/
# edit TV_ADDR / ADB_BIN in the unit if needed:
sudo systemctl daemon-reload
sudo systemctl enable --now keybridge-watch
journalctl -u keybridge-watch -f      # watch it run
```

Press the gear button on the TV → the Picture pane opens. Volume long-press still ramps.

## Notes
- If the TV reboots, the watcher reconnects automatically (and re-auth is not needed once
  "Always allow" was accepted).
- Latency is sub-second.
- This is the "headless companion" approach. For a self-contained, publishable on-TV app, see
  the Shizuku route in the project README (TODO).
