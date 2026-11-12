# Development Container Setup

This directory contains configuration for a comprehensive development environment supporting multiple languages and tools.

## Quick Start

### Prerequisites
- Docker Desktop or Docker Engine
- Visual Studio Code with Remote - Containers extension

### Launch Dev Container
1. Open the project in VS Code
2. Press `Ctrl+Shift+P` (or `Cmd+Shift+P` on Mac)
3. Select "Dev Containers: Reopen in Container"
4. Wait for the container to build and setup

## Included Runtimes & Tools

### Languages
- **Node.js** 20 LTS - JavaScript/TypeScript development
- **Python** 3.11 - Python development with linting & testing tools
- **Go** 1.23 - Go development with linters
- **Java** 21 - Java development with Maven & Gradle
- **Docker** - Docker-outside-Docker for container operations

### Development Tools
- **GitHub CLI** - GitHub automation and management
- **npm, yarn, pnpm** - JavaScript package managers
- **Maven & Gradle** - Java build tools
- **git** - Version control

### Python Packages (Pre-installed)
- black - Code formatter
- pylint - Linter
- pytest - Testing framework
- flake8 - Style guide enforcement
- pytest-cov - Code coverage
- mypy - Type checker

### VS Code Extensions
- TypeScript/JavaScript support
- Python extension pack
- Go extension
- Java extension pack
- YAML support
- Prettier code formatter
- GitHub Copilot
- Remote containers

## Forwarded Ports

| Port | Service | Label |
|------|---------|-------|
| 3000 | Application | App server |
| 5000 | Flask/API | Python API |
| 8000 | Dev Server | Node dev server |
| 8080 | HTTP | Web server |
| 8443 | HTTPS | Secure web server |
| 9000 | Java Debug | Debug port |
| 27017 | MongoDB | NoSQL database |
| 5432 | PostgreSQL | SQL database |
| 6379 | Redis | Cache/session store |

## Environment Variables

```bash
NODE_ENV=development
PYTHONUNBUFFERED=1
GOPATH=/home/node/go
TZ=UTC
```

## File Mounts

- `~/.ssh` → `/home/node/.ssh` (read-only)
- `~/.gitconfig` → `/home/node/.gitconfig` (read-only)
- `~/.m2` → `/home/node/.m2` (Maven cache)

## Common Commands

### Node.js
```bash
node --version
npm install
npm run dev
npm test
```

### Python
```bash
python --version
pip install -r requirements.txt
python -m pytest
python -m black .
python -m pylint *.py
```

### Go
```bash
go version
go mod tidy
go build ./...
golangci-lint run ./...
```

### Java
```bash
java -version
mvn clean install
gradle build
```

### Docker
```bash
docker ps
docker build .
docker run -it <image>
```

## Customization

### Adding VS Code Extensions
Edit `.devcontainer/devcontainer.json` and add to the `extensions` array:
```json
"customizations": {
  "vscode": {
    "extensions": [
      "publisher.extension-name"
    ]
  }
}
```

### Adding System Packages
Update `.devcontainer/Dockerfile` RUN command:
```dockerfile
RUN apt-get update && apt-get install -y \
    package-name \
    && rm -rf /var/lib/apt/lists/*
```

### Adding Environment Variables
Edit `devcontainer.json` `remoteEnv` section or update `post-create.sh`

## Troubleshooting

### Container won't start
- Check Docker is running: `docker ps`
- Rebuild container: `Dev Containers: Rebuild Container`

### Port conflicts
- Modify `forwardPorts` in `devcontainer.json`
- Or use `portsAttributes` to change auto-forward behavior

### Permission issues
- Ensure `/workspace` directories are writable
- Check mounted volumes have correct permissions

### Git credentials not working
- Verify `~/.gitconfig` exists and is mounted
- Consider using SSH keys from `~/.ssh`

## Resources

- [Dev Containers Documentation](https://containers.dev/)
- [Node.js LTS](https://nodejs.org/)
- [Python 3.11](https://www.python.org/)
- [Go 1.23](https://go.dev/)
- [Java 21](https://www.oracle.com/java/)
