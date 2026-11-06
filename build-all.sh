#!/usr/bin/env sh
# Builds one jar per supported Minecraft version into build/libs/.
# Format: <minecraft_version> <fabric_api_version> <fabric.mod.json minecraft range>
set -e
./gradlew clean
while read -r mc api compat; do
  [ -z "$mc" ] && continue
  echo "== Minecraft $mc"
  ./gradlew build -Pminecraft_version="$mc" -Pfabric_api_version="$api" -Pmc_compat="$compat"
done <<'VERSIONS'
26.1.2 0.155.3+26.1.2 ~26.1
26.2 0.161.0+26.2 ~26.2
26.3 0.165.2+26.3 ~26.3
VERSIONS
ls build/libs
