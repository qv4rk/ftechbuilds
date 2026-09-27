# TrendPulse 1.0 beta source for Termux

This package installs as `com.feisttech.trendpulse`. The public repository uses generic vector icon and splash resources and a replaceable `assets/loading.gif`. The offline source ZIP preserves the selected TrendPulse artwork.

## Build on the phone

```sh
cd ~/trendpulse-v1
export ANDROID_HOME="$HOME/Android/Sdk"
chmod +x build-apk.sh
# Optional: use the animated file you moved to Termux home
cp "$HOME/pulsing_load.gif" assets/loading.gif
./build-apk.sh
cp build/TrendPulse-1.0.0-beta.1.apk "$HOME/storage/downloads/"
```

The default key is `$HOME/.trendpulse-v1-signing.keystore`. The script checks the key password before compiling, preserves an existing key on mismatch, and creates a new key only if that path does not exist. Keep the key and password for subsequent updates. Never post either publicly. A password is never printed by the script.

## Working flow

- Import a case plan CSV. Tap **Cases** to choose a named case; **Queries** shows the exact term group, geo, and category for each row. Position persists across restarts. Import uses the case plan's original bytes.
- Tap **Languages** to enter reviewed strings in English, Hebrew, Spanish, French, Arabic, and Chinese. The six languages produce two separate groups of up to five quoted strings. Automatic translation suggestions are planned but not integrated.
- Tap **Open** for the app browser or **Browser** to inspect in an external browser. Settings switches desktop/mobile page view.
- Tap **Capture page** to store a full-document image in **Collected**. The app saves an immutable raw PNG, a stamped PNG with UTC time and URL, manifest and hashes, plus an optional original CSV. Open **Collected** to review, export a capture ZIP, or delete it. Export is independent of capture.
- Tap **Run** and select a delay to load, capture, and move through the current queue while the app stays open. The selected position survives restart; a 429 pauses requests. Tap Run again to stop.
- Tap **Menu / Hide** to expand or collapse the controls; rotate the phone to inspect the chart with more space. Both visual skins use the same saved collection.

## Current Android validation boundary

This beta needs a build and capture test on the target phone. Google Trends may change its dynamic page; check every saved raw image and CSV before relying on a run. Background execution, scheduled recurrence, translated suggestions, OCR, multiple live tabs, app lock, and project merge/export remain in the full 1.0 specification (`SPEC.md`). The active Run stays visible to the user and requires the app to remain open.

The included sample case plan contains synthetic market-research terms. Personal case plans and captured data remain on your phone unless you export them.
