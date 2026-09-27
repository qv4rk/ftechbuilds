#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail
cd "$(dirname "$0")"
ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
ANDROID_JAR="$ANDROID_HOME/platforms/android-35/android.jar"
D8="${D8:-$ANDROID_HOME/cmdline-tools/latest/bin/d8}"
if [ ! -x "$D8" ]; then D8="$ANDROID_HOME/build-tools/35.0.0/d8"; fi
KEYSTORE="${TRENDS_KEYSTORE:-$HOME/.trendpulse-v1-signing.keystore}"
for c in javac aapt2 apksigner keytool zip unzip; do command -v "$c" >/dev/null || { echo "Missing $c" >&2; exit 1; }; done
for f in "$ANDROID_JAR" "$D8" AndroidManifest.xml assets/loading.gif src/com/feisttech/trendsevidence/MainActivity.java src/com/feisttech/trendsevidence/LanguagePlanner.java src/com/feisttech/trendsevidence/CasePlan.java src/com/feisttech/trendsevidence/CaptureStore.java; do test -s "$f" || { echo "Missing $f" >&2; exit 1; }; done
if [ -z "${TRENDS_KEYSTORE_PASSWORD:-}" ]; then
  read -r -s -p 'Development keystore password: ' TRENDS_KEYSTORE_PASSWORD
  echo
fi
[ "${#TRENDS_KEYSTORE_PASSWORD}" -ge 6 ] || { echo 'Password must be at least 6 characters' >&2; exit 1; }
[ "$(wc -c < assets/loading.gif)" -le 8388608 ] || { echo "loading.gif exceeds the 8 MiB build limit" >&2; exit 1; }
[ "$(head -c 3 assets/loading.gif)" = GIF ] || { echo "assets/loading.gif must be a real GIF file" >&2; exit 1; }
export TRENDS_KEYSTORE_PASSWORD
if [ -f "$KEYSTORE" ]; then
  if ! keytool -list -keystore "$KEYSTORE" -storepass "$TRENDS_KEYSTORE_PASSWORD" >/dev/null 2>&1; then
    echo "The existing signing key at $KEYSTORE does not accept this password. Choose a fresh path with TRENDS_KEYSTORE or enter that key's original password. The key has been preserved." >&2
    exit 1
  fi
else
  umask 077
  keytool -genkeypair -keystore "$KEYSTORE" -storepass "$TRENDS_KEYSTORE_PASSWORD" -keypass "$TRENDS_KEYSTORE_PASSWORD" -alias trendsdev -keyalg RSA -keysize 3072 -validity 10000 -dname 'CN=Trends Evidence Development, O=FeistTech, C=US' >/dev/null
fi
mkdir -p build/classes build/dex build/res
aapt2 compile --dir res -o build/res
javac -source 8 -target 8 -classpath "$ANDROID_JAR" -d build/classes src/com/feisttech/trendsevidence/MainActivity.java src/com/feisttech/trendsevidence/LanguagePlanner.java src/com/feisttech/trendsevidence/CasePlan.java src/com/feisttech/trendsevidence/CaptureStore.java
mapfile -t classes < <(find build/classes -name '*.class' -type f | sort)
[ "${#classes[@]}" -gt 0 ] || { echo 'No class files generated' >&2; exit 1; }
"$D8" --lib "$ANDROID_JAR" --min-api 24 --output build/dex "${classes[@]}"
test -s build/dex/classes.dex
mapfile -t resources < <(find build/res -name '*.flat' -type f | sort)
[ "${#resources[@]}" -gt 0 ] || { echo 'No compiled resources' >&2; exit 1; }
resource_args=()
for resource in "${resources[@]}"; do resource_args+=(-R "$resource"); done
aapt2 link -I "$ANDROID_JAR" -A assets "${resource_args[@]}" --manifest AndroidManifest.xml --min-sdk-version 24 --target-sdk-version 35 --version-code 11 --version-name 1.0.0-beta.1 -o build/base.apk
unzip -Z1 build/base.apk | grep -qx AndroidManifest.xml
unzip -Z1 build/base.apk | grep -qx resources.arsc
aapt2 dump badging build/base.apk | grep -q 'com.feisttech.trendpulse'
cp build/base.apk build/unsigned.apk
(cd build/dex && zip -q ../unsigned.apk classes.dex)
unzip -Z1 build/unsigned.apk | grep -qx classes.dex
cp build/unsigned.apk build/TrendPulse-1.0.0-beta.1.apk
apksigner sign --ks "$KEYSTORE" --ks-key-alias trendsdev --ks-pass env:TRENDS_KEYSTORE_PASSWORD --key-pass env:TRENDS_KEYSTORE_PASSWORD build/TrendPulse-1.0.0-beta.1.apk
apksigner verify --verbose build/TrendPulse-1.0.0-beta.1.apk
aapt2 dump badging build/TrendPulse-1.0.0-beta.1.apk | grep -E 'package:|sdkVersion:|targetSdkVersion:|launchable-activity:'
echo 'Built: build/TrendPulse-1.0.0-beta.1.apk'
