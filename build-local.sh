#!/bin/bash

echo "Iniciando compilação local do Front Launcher..."

# Garante permissão de execução no gradlew
chmod +x ./gradlew

# Auto-incrementa a versão local
VERSION_FILE="version.properties"
if [ ! -f "$VERSION_FILE" ]; then
    echo "VERSION_CODE=1" > "$VERSION_FILE"
fi
V_CODE=$(grep VERSION_CODE $VERSION_FILE | cut -d '=' -f2)
NEW_V_CODE=$((V_CODE + 1))
echo "VERSION_CODE=$NEW_V_CODE" > "$VERSION_FILE"
echo ">> Atualizando versão local do APK para 1.$NEW_V_CODE..."

# Executa a compilação padrão
./gradlew assembleDebug

# Copia o APK para a raiz renomeado
cp app/build/outputs/apk/debug/app-debug.apk ./FrontLauncher-debug.apk

# Verifica se o arquivo foi gerado corretamente
if [ -f "FrontLauncher-debug.apk" ]; then
    echo "=========================================================="
    echo "✅ Sucesso! O APK foi gerado e exportado com sucesso."
    echo "Caminho do arquivo: $(pwd)/FrontLauncher-debug.apk"
    echo "=========================================================="
else
    echo "❌ Erro: O arquivo FrontLauncher-debug.apk não foi encontrado."
    exit 1
fi
