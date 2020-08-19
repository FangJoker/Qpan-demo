package com.chavez.qpan;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author Chavez Qiu
 * @Date 19-12-24.
 * Email：qiuhao1@meizu.com
 * Description：
 */
public class ThreadExecutor extends ThreadPoolExecutor {

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger atomicInteger = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "download-thread-" + atomicInteger.getAndIncrement());
        }
    };


    public ThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, sThreadFactory);
    }

    private ThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory,new ThreadPoolExecutor.DiscardPolicy());
    }

}
