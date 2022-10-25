ARG IMAGE
FROM $IMAGE

RUN mkdir -p /usr/share/man/man1 # https://github.com/debuerreotype/docker-debian-artifacts/issues/24
RUN (apk update && apk add linux-headers g++ make openjdk11 maven git unzip) || true
RUN (apt update && DEBIAN_FRONTEND=noninteractive apt install -y --no-install-recommends build-essential make openjdk-11-jdk-headless maven git unzip) || true