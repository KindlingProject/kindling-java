#!/bin/bash
# Copyright 2022 The Kindling Authors
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -eu

export PROFILER_BRANCH=kindling
export DIR=$(cd `dirname $0`; pwd)

run_docker() {
  ARCH=$1
  case $ARCH in
    linux-x64)
      IMAGE="centos:centos7"
      PLATFORM="linux/amd64"
      ;;
    linux-x64-musl)
      IMAGE="alpine"
      PLATFORM="linux/amd64"
      ;;
    linux-arm64)
      IMAGE="arm64v8/alpine"
      PLATFORM="linux/arm64"
      ;;
    *)
      echo "Only linux-x64, linux-x64-musl and linux-arm64 are valid arch options."
      exit 0
      ;;
  esac

  # Build Image.
  TAG="kindling-java-build:${ARCH}"
  if [ -z "$(docker images | grep kindling-java-build | grep ${ARCH})" ];then
    docker build --build-arg IMAGE=${IMAGE} -t ${TAG} --platform ${PLATFORM} ${DIR}/docker
  fi

  # Start Docker Build
  docker run --rm -it --platform ${PLATFORM} -v ${DIR}:/data/src/kindling-java -v ${DIR}/docker/build.sh:/data/script/build.sh -v ${DIR}/docker/build_in_docker.sh:/data/script/build_in_docker.sh ${TAG} bash -eux /data/script/build_in_docker.sh ${PROFILER_BRANCH}
}

# Build Local             ./build.sh
# Build Docker(X64)       ./build.sh linux-x64
# Build Docker(X64-MUSL)  ./build.sh linux-x64-musl
# Build Docker(ARM64)     ./build.sh linux-arm64
if [ $# == 0 ]; then
  . ${DIR}/docker/build.sh
  buildLocal ${DIR} ${PROFILER_BRANCH}
else
  run_docker $1
fi