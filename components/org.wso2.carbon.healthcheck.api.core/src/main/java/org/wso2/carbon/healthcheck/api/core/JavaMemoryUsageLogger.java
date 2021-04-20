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


import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.EnumMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service which logs the memory usage of carbon server.
 */
public class JavaMemoryUsageLogger {

    private static final int BYTES_PER_MB = 1024 * 1000;
    private static MemoryMXBean memoryMXBean;

    private static final Log log = LogFactory.getLog(JavaMemoryUsageLogger.class);
    private int delaySecondsBeforeStarting = Constants.MemoryUsageLoggerConfig.DEFAULT_WAIT_SECONDS;
    private int loggerPrintingInterval = Constants.MemoryUsageLoggerConfig.DEFAULT_INTERVAL_SECONDS;
    private ScheduledExecutorService memoryLogScheduler;
    private ScheduledFuture<MemoryUsageReporter> scheduledFuture;

    public JavaMemoryUsageLogger() {

    }

    public JavaMemoryUsageLogger(int loggerPrintingInterval) {

        this.loggerPrintingInterval = loggerPrintingInterval;
        this.delaySecondsBeforeStarting = loggerPrintingInterval * 2;
        memoryLogScheduler = Executors.newScheduledThreadPool(1);
    }

    public void start() {

        memoryMXBean = ManagementFactory.getMemoryMXBean();
        scheduledFuture = (ScheduledFuture<MemoryUsageReporter>) memoryLogScheduler.
                scheduleAtFixedRate(new MemoryUsageReporter(), delaySecondsBeforeStarting, loggerPrintingInterval, TimeUnit.SECONDS);
    }

    public void stop() {

        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            memoryLogScheduler.shutdown();
        }
    }

    /**
     * Memory Usage Reporter.
     */
    public static class MemoryUsageReporter implements Runnable {

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

            formatAndWrite("JVM Memory Usage: Heap Used: %dM, Heap Committed: %dM, Heap Max: %dM, " +
                            "Non Heap Used: %dM, Non Heap Committed: %dM, Non Heap Max: %dM",
                    attributes.get(Attribute.heap_memory_used_mb),
                    attributes.get(Attribute.heap_memory_committed_mb),
                    attributes.get(Attribute.heap_memory_max_mb),
                    attributes.get(Attribute.nonheap_memory_used_mb),
                    attributes.get(Attribute.nonheap_memory_committed_mb),
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

        private void formatAndWrite(String fmt, Object... args) {

            String msg = String.format(fmt, args);
            log.info(msg);
        }
    }
}
