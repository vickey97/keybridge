# KeyBridge — project state / handoff notes

Goal: make the TCL remote's **gear / notification button** open the **Picture settings pane**
(`android.settings.DISPLAY_SETTINGS`) as an overlay during playback (so HDR/Dolby Vision picture
modes show with live preview) — **without breaking volume hold-to-ramp.**

## Hardware / facts
- TV: **TCL C7K**, Google TV, **Android 14 (build V643 / AU02)**. ADB at **`192.168.178.41:5555`**.
- Gear button = `KEYCODE_NOTIFICATION` (scanCode 240). It fires `TOGGLE_NOTIFICATION_HANDLER_PANEL`;
  with Google TV launcher disabled, SystemUI logs:
  `E/TvNotificationPanel: ... Could not resolve activityInfo for intent ...TOGGLE_NOTIFICATION_HANDLER_PANEL`
- Opening the pane: `am start -a android.settings.DISPLAY_SETTINGS` → resolves to
  `com.android.tv.settings/com.tcl.settings.PictureActivity`, shown as an overlay (content stays behind).

## What we proved (so we don't repeat it)
- **Accessibility remappers break volume** hold-to-ramp (global key filtering). Only fix without
  shell access is the clunky "press-once-then-hold." Smooth volume needs shell-level `getevent`.
- **v1 — intent-handler app**: registering an activity for `TOGGLE_NOTIFICATION_HANDLER_PANEL`
  resolves via `resolveActivity`, but **SystemUI refuses to launch a non-system (sideloaded) app**
  as the handler. Dead end.
- **v2/v3 — in-app logcat watcher**: this TCL build **won't let a sideloaded app read system logs**
  even with `READ_LOGS` granted + `log` gid 1007 + a reboot (logd returns only the app's own lines).
  Dead end.
- An app **cannot self-grant shell privileges** — that bridge must be an external ADB-started
  process (which is exactly what **Shizuku** is).

## Working solutions
- **B) Server watcher (this repo: `server-watcher/`)** — smooth volume, nothing on the TV, no
  accessibility. Needs an always-on box with ADB to the TV. **This is the chosen path to deploy on
  the Ubuntu mini PC.**
- **C) Shizuku app (TODO)** — self-contained, publishable; uses Shizuku for shell-level log access.
  Needs Shizuku installed + once-per-reboot activation. Standard pattern (Button Mapper / Key
  Mapper / tvQuickActions all use Shizuku).

## Next step
Deploy `server-watcher/` to the Ubuntu mini PC as a systemd service (see its README). Then,
optionally, build the Shizuku version (C) for publishing.

## TV state recap (debloat / launcher)
- Projectivy is the launcher via Launcher Manager (HOME role); Google TV launcher (`launcherx`)
  disabled. `com.tcl.tvinput` MUST stay enabled (HDR/Dolby Vision). `com.tcl.ttvs` MUST stay
  enabled (System Update). Full details in the user's memory file `tcl-c7k-debloat.md`.
