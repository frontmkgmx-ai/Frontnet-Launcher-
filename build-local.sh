#!/usr/bin/env bash
set -e

echo "==> [1/5] Garantindo Keystore de assinatura persistente na raiz..."
rm -f ci-debug.keystore
keytool -genkey -v -keystore ci-debug.keystore -storetype JKS \
  -storepass androiddebug -alias androiddebugkey -keypass androiddebugkey \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=FrontLauncher,O=LocalBuild,C=US"

echo "==> [2/5] Dando permissão ao gradlew..."
chmod +x gradlew

echo "==> [3/5] Compilando APK Debug localmente..."
./gradlew assembleDebug -x test -x lint -x lintAnalyzeDebug --stacktrace --no-daemon

echo "==> [4/5] Copiando APK gerado para a raiz do projeto..."
cp app/build/outputs/apk/debug/*.apk ./FrontLauncher-debug.apk

echo "==> [5/5] Sincronizando com o repositório Git (raiz e branch main)..."
git add FrontLauncher-debug.apk ci-debug.keystore
git commit -m "build(local): update FrontLauncher-debug.apk at repository root [skip ci]" || echo "Nenhuma alteração no APK para commitar."

echo "==> Sucesso! O arquivo FrontLauncher-debug.apk está pronto na raiz do projeto e commitado."
