/*
 * Copyright 2022 The Kindling Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.kindling.agent.profiler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.kindling.agent.deps.one.profiler.AsyncProfiler;
import io.kindling.agent.util.DefaultThreadFactory;
import io.kindling.agent.util.FileWriter;

public class Profiler {
	private final long intervalMs;
	private final int depth;
	private final AsyncProfiler instance;
	private boolean profilingStarted = false;
	private ScheduledExecutorService executor;

	public Profiler(long intervalMs, int depth, String libPath) {
		this.intervalMs = intervalMs;
		this.depth = depth;
		this.instance = libPath == null ? null : AsyncProfiler.getInstance(libPath);
		this.executor = Executors.newSingleThreadScheduledExecutor(new DefaultThreadFactory("Kindling AsyncProfiler", true));
	}

	public synchronized void start(FileWriter writer) throws Exception {
		if (instance == null) {
			writer.log("Fail to Start Async Profiler");
			return;
		}
		AsyncProfilerStarter.start(this.instance, intervalMs, depth, writer);
		this.profilingStarted = true;
		executor.scheduleAtFixedRate(new Runnable() {
			public void run() {
				try {
					dump();
				} catch (Throwable cause) {
					cause.printStackTrace();
				}
			}
		}, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
	}

	public synchronized void stop() throws Exception {
		if (instance == null) {
			return;
		}

		if (executor != null) {
			executor.shutdown();
		}
		this.instance.stop();
	}

	final synchronized void dump() throws Exception {
		if (this.profilingStarted == false) {
			throw new IllegalStateException("Profiling is not started");
		}
		this.instance.execute("print");
	}
}
