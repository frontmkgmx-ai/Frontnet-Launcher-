#!/usr/bin/env bash
set -e
mkdir -p app
if [ ! -f "app/ci-debug.keystore" ]; then
  keytool -genkey -v -keystore app/ci-debug.keystore \
    -storepass androiddebug -alias androiddebugkey -keypass androiddebugkey \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=FrontLauncher,O=LocalBuild,C=US"
fi
chmod +x gradlew
./gradlew assembleDebug -x test -x lint -x lintAnalyzeDebug --stacktrace --no-daemon
cp app/build/outputs/apk/debug/*.apk ./FrontLauncher-debug.apk
git add FrontLauncher-debug.apk app/ci-debug.keystore
git diff --staged --quiet || git commit -m "build(local): update FrontLauncher-debug.apk at root [skip ci]" || true
echo "Sucesso: FrontLauncher-debug.apk pronto na raiz!"
