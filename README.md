# KeyBridge

Make Android TV / Google TV remote buttons open whatever you want — **without an accessibility
service**, so volume long-press and other system keys keep working normally.

KeyBridge works by registering for the *system intents* that certain remote buttons fire. For
example, on TCL the **gear / notification button** fires `TOGGLE_NOTIFICATION_HANDLER_PANEL`. When
that button is pressed, the OS launches KeyBridge, which immediately opens your chosen target and
gets out of the way.

## Current behaviour (v1)
- **Gear / notification button → Picture settings pane** (`android.settings.DISPLAY_SETTINGS`),
  shown as an overlay so HDR / Dolby Vision picture modes stay live with on-screen preview.

## Why no accessibility?
Accessibility-based button mappers must filter *all* key events, which breaks volume hold-to-ramp on
most TVs. KeyBridge never touches the key stream — it only answers a system intent — so volume is
untouched.

## Build (no local SDK needed)
Push to GitHub → the **Build APK** workflow compiles `app-debug.apk` and uploads it as the
`keybridge-apk` artifact. Download it from the workflow run.

## Install
```
adb install -r app-debug.apk
```

## Roadmap
- Map additional remote buttons that expose system intents.
- Optional, per-button log-watch mode for buttons that don't expose an intent.
- Simple in-app picker for the target action.

## License
MIT
