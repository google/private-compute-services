# This Dockerfile extends the universal dev container with custom setup
# For most use cases, the devcontainer.json features are sufficient

FROM mcr.microsoft.com/devcontainers/universal:2

# Install additional system tools
RUN apt-get update && apt-get install -y \
    build-essential \
    curl \
    wget \
    git \
    vim \
    nano \
    htop \
    tmux \
    jq \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Create workspace directories
RUN mkdir -p /workspace/{go,python,node,java} && \
    chown -R node:node /workspace

USER node
WORKDIR /home/node

# Set up shell environment
RUN echo 'export PATH=$PATH:/home/node/go/bin' >> ~/.bashrc && \
    echo 'export GOPATH=/home/node/go' >> ~/.bashrc
