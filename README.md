# kindling-java
Kindling-java is an attach agent to collect Java CPU / LOCK for probe which relies on async-profiler.

## HowTo Build
* Download [AsyncProfiler](https://github.com/CloudDectective-Harmonycloud/async-profiler)

* Build Kindling Java & AsyncProfiler

> ./build.sh $async_profiler_dir

```
async-profiler-2.8.3-linux-x64
├── agent
│   ├── 1.0.0
│   │   ├── agent-core.jar
│   │   └── plugin-traceid-sw.jar
│   ├── agent-boot.jar
│   └── version
├── build
│   ├── async-profiler.jar
│   ├── converter.jar
│   ├── fdtransfer
│   ├── jattach
│   └── libasyncProfiler.so
├── CHANGELOG.md
├── LICENSE
├── profiler.sh
└── README.md
```

## HowTo Use
* Attach Java

> ./profiler.sh start $pid

* Detach Java

> ./profiler.sh stop $pid