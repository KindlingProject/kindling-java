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

get_ext_name()
{
	ARCH="$(shell uname -m)"
	if [ ${ARCH} -eq "x86_64" ]; then
		return "zip"
	else
		return "tar.gz"
	fi
}

PROFILER_BRANCH=get-cpu-lock-event-sw
DIR=$(cd `dirname $0`; pwd)
PROFILER_PATH=${DIR}/build/async-profiler

if [ ! -d "${DIR}/build/" ]; then
	mkdir build
fi

if [ ! -d "${PROFILER_PATH}" ]; then
	cd build
	wget https://github.com/CloudDectective-Harmonycloud/async-profiler/archive/refs/heads/${PROFILER_BRANCH}.zip
	unzip ${PROFILER_BRANCH}.zip
	mv async-profiler-${PROFILER_BRANCH} async-profiler
	rm -f ${PROFILER_BRANCH}.zip
	cd ${DIR}
fi

if [ ! -d "${PROFILER_PATH}/agent" ]; then
	# Build agent-package-xxx.zip
	mvn clean package
	
	VERSION=$(cat agent-package/target/classes/version)
	echo "Agent Version: ${VERSION}"
	
	# Copy Agent To AsyncProfiler
	cd agent-package/target
	unzip agent-package-${VERSION}.zip
	cp -Rf agent-package ${PROFILER_PATH}/agent
fi


cd ${PROFILER_PATH}
make release
rm -rf agent
rm -rf build

# Copy binary to build folder.
mv async-profiler-*.* ${DIR}/build