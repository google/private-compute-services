#!/bin/bash
set -e

echo "🚀 Starting post-create setup..."

# Update system packages
echo "📦 Updating system packages..."
sudo apt-get update
sudo apt-get upgrade -y

# Node.js setup
echo "📦 Setting up Node.js..."
npm install -g npm@latest
npm install -g yarn pnpm

# Python setup
echo "🐍 Setting up Python..."
pip install --upgrade pip setuptools wheel
pip install black pylint pytest flake8 pytest-cov mypy

# Go setup
echo "🐹 Setting up Go..."
go install github.com/golangci/golangci-lint/cmd/golangci-lint@latest
go install golang.org/x/tools/...@latest

# Java setup
echo "☕ Setting up Java..."
mkdir -p ~/.m2

# Git setup
echo "🔧 Configuring Git..."
git config --global core.editor "nano"
git config --global pull.rebase false

# Docker setup
echo "🐳 Verifying Docker..."
docker --version

# Create workspace directories
echo "📁 Creating workspace directories..."
mkdir -p /workspace/go
mkdir -p /workspace/python
mkdir -p /workspace/node
mkdir -p /workspace/java

echo "✅ Post-create setup complete!"
echo ""
echo "Available tools:"
echo "  Node.js: $(node --version)"
echo "  NPM: $(npm --version)"
echo "  Yarn: $(yarn --version)"
echo "  Python: $(python --version)"
echo "  Go: $(go version)"
echo "  Java: $(java -version 2>&1 | head -n 1)"
echo "  Maven: $(mvn --version 2>&1 | head -n 1)"
echo "  Gradle: $(gradle --version 2>&1 | head -n 1)"
echo "  GitHub CLI: $(gh --version 2>&1 | head -n 1)"
echo ""
