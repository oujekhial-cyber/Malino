#!/usr/bin/env bash
set -euo pipefail
REPO="${1:-Malino}"
USER="oujekhial-cyber"
cd "$(dirname "$0")"
git init
git branch -M main
git remote remove origin 2>/dev/null || true
git remote add origin "git@github.com:${USER}/${REPO}.git"
git add .
git commit -m "Malino 1.1 PySide6 Android" || true
git push -u origin main
echo "Pushed to git@github.com:${USER}/${REPO}.git"
