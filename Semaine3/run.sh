#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"
mkdir -p out
javac -d out src/main/java/semaine3/*.java

echo "Build OK."
echo "Run in separate terminals:"
echo "  java -cp out semaine3.Main 0"
echo "  java -cp out semaine3.Main 1"
echo "  java -cp out semaine3.Main 2"
