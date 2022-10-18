#!/bin/bash
if [ ! -n "$1" ] ;then
	echo "AsyncProfiler Path is not set. Use following command: /build.sh <async-profiler-path>"
	exit 1
fi

export ASYNC_PROFILER_PATH=$1

# Build agent-apckage-xxx.zip
mvn clean package

VERSION=$(cat agent-package/target/classes/version)
echo "Agent Version: ${VERSION}"

# Copy Agent To AsyncProfiler
cd agent-package/target
unzip agent-package-${VERSION}.zip
cp -R agent-package ${ASYNC_PROFILER_PATH}/agent

# Release AsyncProfiler
cd ${ASYNC_PROFILER_PATH}
make release