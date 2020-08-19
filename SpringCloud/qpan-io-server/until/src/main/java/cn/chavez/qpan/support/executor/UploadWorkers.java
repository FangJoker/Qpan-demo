package cn.chavez.qpan.support.executor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/4/9 2:24
 */
public class UploadWorkers extends ThreadPoolExecutor {
    private final static int DEFAULT_CORE_POOL_SIZE = 2;
    private final static int DEFAULT_MAX_MUM_CORE_POOL_SIZE = 2;
    private final static long DEFAULT_KEEP_ALIVE_TIME = 60L;
    private final static TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;
    private static ArrayBlockingQueue workQueue = new ArrayBlockingQueue(1000);
    private static ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("upload-pool-%d")
            .build();


    public UploadWorkers(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public UploadWorkers() {
        super(DEFAULT_CORE_POOL_SIZE, DEFAULT_MAX_MUM_CORE_POOL_SIZE, DEFAULT_KEEP_ALIVE_TIME, DEFAULT_TIME_UNIT, workQueue, threadFactory);
    }


}

