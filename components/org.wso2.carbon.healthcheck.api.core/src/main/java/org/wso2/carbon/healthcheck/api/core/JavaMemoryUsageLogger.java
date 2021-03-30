/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.healthcheck.api.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.management.*;
import java.util.EnumMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service which logs the memory usage of carbon server.
 */
public class JavaMemoryUsageLogger {

    static int BYTES_PER_MB = 1024 * 1000;
    private static MemoryMXBean memoryMXBean;

    private static final Log log = LogFactory.getLog(JavaMemoryUsageLogger.class);
    int wait = Constants.MemoryUsageLoggerConfig.DEFAULT_WAIT_SECONDS;
    int interval = Constants.MemoryUsageLoggerConfig.DEFAULT_INTERVAL_SECONDS;
    ScheduledExecutorService ses;
    ScheduledFuture<?> scheduledFuture;

    public JavaMemoryUsageLogger() {

    }

    public JavaMemoryUsageLogger(int wait, int interval) {

        this.wait = wait;
        this.interval = interval;
        ses = Executors.newScheduledThreadPool(1);
    }

    public void start() {

        memoryMXBean = ManagementFactory.getMemoryMXBean();
        scheduledFuture = ses.scheduleAtFixedRate(new Reporter(), wait, interval, TimeUnit.SECONDS);
    }

    public void stop() {

        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            ses.shutdown();
        }
    }

    /**
     * Class to log memory utilization periodically.
     */
    public static class Reporter implements Runnable {

        enum Attribute {
            heap_memory_used_mb("Heap Memory Used"),
            heap_memory_committed_mb("Heap Memory Committed"),
            heap_memory_max_mb("Heap Memory Max"),
            nonheap_memory_used_mb("Non-Heap Memory Used"),
            nonheap_memory_committed_mb("Non-Heap Memory Committed"),
            nonheap_memory_max_mb("Non-Heap Memory Maximum"),
            mapped_buffers_count("Mapped Buffers Count");

            public final String name;

            Attribute(String name) {
                this.name = name;
            }
        }

        @Override
        public void run() {

            printUserReport(getMemoryUtilization());
        }

        private void printUserReport(EnumMap<Attribute, Long> attributes) {

            formatAndOutput("JVM Memory Usage     (Heap): used: %dM committed: %dM max:%dM",
                    attributes.get(Attribute.heap_memory_used_mb), attributes.get(Attribute.heap_memory_committed_mb),
                    attributes.get(Attribute.heap_memory_max_mb));
            formatAndOutput("JVM Memory Usage (Non-Heap): used: %dM committed: %dM max:%dM",
                    attributes.get(Attribute.nonheap_memory_used_mb), attributes.get(Attribute.nonheap_memory_committed_mb),
                    attributes.get(Attribute.nonheap_memory_max_mb));
        }


        private EnumMap<Attribute, Long> getMemoryUtilization() {

            EnumMap<Attribute, Long> attributes = new EnumMap<Attribute, Long>(Attribute.class);
            MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
            MemoryUsage nonHeap = memoryMXBean.getNonHeapMemoryUsage();
            attributes.put(Attribute.heap_memory_used_mb, heap.getUsed() / BYTES_PER_MB);
            attributes.put(Attribute.heap_memory_committed_mb, heap.getCommitted() / BYTES_PER_MB);
            attributes.put(Attribute.heap_memory_max_mb, heap.getMax() / BYTES_PER_MB);
            attributes.put(Attribute.nonheap_memory_used_mb, nonHeap.getUsed() / BYTES_PER_MB);
            attributes.put(Attribute.nonheap_memory_committed_mb, nonHeap.getCommitted() / BYTES_PER_MB);
            attributes.put(Attribute.nonheap_memory_max_mb, nonHeap.getMax() / BYTES_PER_MB);
            return attributes;
        }

        private void formatAndOutput(String fmt, Object... args) {

            String msg = String.format(fmt, args);
            log.info(msg);
        }
    }
}
