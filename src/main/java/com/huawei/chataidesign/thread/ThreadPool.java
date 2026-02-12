package com.huawei.chataidesign.thread;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义线程池配置类
 * 基于ThreadPoolExecutor实现，提供生产环境可用的线程池配置
 * 
 * @author ChatAiDesign
 * @since 2024
 */
@Slf4j
@Component
public class ThreadPool {

    /**
     * CPU核心数，用于计算线程池大小
     */
    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();

    /**
     * 核心线程数：线程池中始终保持的最小线程数量
     * 即使这些线程处于空闲状态，也不会被销毁
     * 
     * 计算公式：CPU核心数 + 1
     * 适用于CPU密集型任务
     */
    private static final int CORE_POOL_SIZE = CPU_CORES + 1;

    /**
     * 最大线程数：线程池中允许创建的最大线程数量
     * 当任务队列满了且当前线程数小于maximumPoolSize时，会创建新线程
     * 
     * 计算公式：CPU核心数 * 2 + 1
     * 为IO密集型任务预留更多线程
     */
    private static final int MAXIMUM_POOL_SIZE = CPU_CORES * 2 + 1;

    /**
     * 线程空闲存活时间：当线程数超过核心线程数时，
     * 多余的空闲线程在终止前等待新任务的最长时间
     * 
     * 单位：秒
     * 设置为60秒，平衡资源回收和响应速度
     */
    private static final long KEEP_ALIVE_TIME = 60L;

    /**
     * 时间单位：配合keepAliveTime使用
     * TimeUnit.SECONDS表示以秒为单位
     */
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    /**
     * 工作队列：用于存放等待执行的任务
     * 
     * 这里使用ArrayBlockingQueue（有界队列）：
     * - 防止任务无限堆积导致内存溢出
     * - 队列容量设置为1000，可根据业务调整
     * - 当队列满且线程数达到最大值时，触发拒绝策略
     */
    private static final BlockingQueue<Runnable> WORK_QUEUE = new ArrayBlockingQueue<>(1000);

    /**
     * 线程工厂：用于创建新线程
     * 
     * 自定义线程工厂的好处：
     * - 统一线程命名规范，便于调试和监控
     * - 设置守护线程属性
     * - 可以设置线程优先级
     */
    private static final ThreadFactory THREAD_FACTORY = new CustomThreadFactory();

    /**
     * 拒绝策略：当线程池和队列都满时的处理策略
     * 
     * 这里使用CallerRunsPolicy：
     * - 不会丢弃任务也不会抛出异常
     * - 由调用线程（主线程）来执行任务
     * - 能够减缓新任务的提交速度
     * - 保证所有任务最终都能被执行
     * 
     * 其他可选策略：
     * - AbortPolicy：直接抛出RejectedExecutionException（默认）
     * - DiscardPolicy：直接丢弃任务，不抛出异常
     * - DiscardOldestPolicy：丢弃队列中最老的任务，然后重新尝试执行
     */
    private static final RejectedExecutionHandler REJECTED_HANDLER = new ThreadPoolExecutor.CallerRunsPolicy();

    /**
     * 实际的线程池实例
     * 使用volatile确保多线程环境下的可见性
     */
    private static volatile ThreadPoolExecutor executor;

    /**
     * 私有构造函数，防止外部实例化
     * 采用单例模式
     */
    private ThreadPool() {}

    /**
     * 获取线程池实例（双重检查锁定单例模式）
     * 
     * @return ThreadPoolExecutor实例
     */
    public static ThreadPoolExecutor getInstance() {
        if (executor == null) {
            synchronized (ThreadPool.class) {
                if (executor == null) {
                    executor = new ThreadPoolExecutor(
                        CORE_POOL_SIZE,        // 核心线程数
                        MAXIMUM_POOL_SIZE,     // 最大线程数
                        KEEP_ALIVE_TIME,       // 空闲线程存活时间
                        TIME_UNIT,             // 时间单位
                        WORK_QUEUE,            // 工作队列
                        THREAD_FACTORY,        // 线程工厂
                        REJECTED_HANDLER       // 拒绝策略
                    );
                    
                    log.info("线程池初始化完成 - 核心线程数: {}, 最大线程数: {}, 队列容量: {}", 
                           CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, WORK_QUEUE.size());
                }
            }
        }
        return executor;
    }

    /**
     * 提交任务到线程池执行
     * 
     * @param task 要执行的任务
     */
    public static void execute(Runnable task) {
        getInstance().execute(task);
    }

    /**
     * 提交有返回值的任务
     * 
     * @param task 要执行的任务
     * @param <T> 返回值类型
     * @return Future对象，可用于获取执行结果
     */
    public static <T> Future<T> submit(Callable<T> task) {
        return getInstance().submit(task);
    }

    /**
     * 获取线程池状态信息
     * 
     * @return 线程池状态字符串
     */
    public static String getPoolStatus() {
        ThreadPoolExecutor pool = getInstance();
        return String.format(
            "活跃线程数: %d, 核心线程数: %d, 最大线程数: %d, " +
            "已完成任务数: %d, 总任务数: %d, 队列大小: %d",
            pool.getActiveCount(),
            pool.getCorePoolSize(),
            pool.getMaximumPoolSize(),
            pool.getCompletedTaskCount(),
            pool.getTaskCount(),
            pool.getQueue().size()
        );
    }

    /**
     * 关闭线程池
     * 
     * shutdown()：平滑关闭，不再接受新任务，但会执行完已提交的任务
     * shutdownNow()：立即关闭，尝试中断正在执行的任务
     */
    public static void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                // 等待最多60秒让现有任务完成
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow(); // 如果超时则强制关闭
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            log.info("线程池已关闭");
        }
    }

    /**
     * 自定义线程工厂类
     * 用于创建具有特定名称和属性的线程
     */
    private static class CustomThreadFactory implements ThreadFactory {
        /**
         * 线程编号原子计数器
         * AtomicInteger保证线程安全的自增操作
         */
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        /**
         * 线程组
         */
        private final ThreadGroup group;

        /**
         * 线程名称前缀
         */
        private final String namePrefix;

        public CustomThreadFactory() {
            // 获取当前线程的线程组
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            
            // 设置线程名称前缀，便于识别和调试
            namePrefix = "chat-ai-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            // 创建新线程
            Thread t = new Thread(group, r,
                namePrefix + threadNumber.getAndIncrement(), // 线程名称：chat-ai-thread-1, chat-ai-thread-2...
                0); // 栈大小，0表示使用默认值
            
            // 设置为非守护线程（应用关闭时会等待此线程执行完毕）
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            
            // 设置线程优先级为正常级别
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            
            // 设置未捕获异常处理器
            t.setUncaughtExceptionHandler((thread, throwable) -> 
                log.error("线程 {} 发生未捕获异常: ", thread.getName(), throwable)
            );
            
            return t;
        }
    }

    /**
     * 示例使用方法
     */
    public static void usageExample() {
        // 方式1：执行无返回值任务
        execute(() -> {
            log.info("执行异步任务，当前线程: {}", Thread.currentThread().getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 方式2：执行有返回值任务
        Future<String> future = submit(() -> {
            log.info("执行带返回值的任务");
            Thread.sleep(2000);
            return "任务执行完成";
        });

        try {
            String result = future.get(3, TimeUnit.SECONDS);
            log.info("任务结果: {}", result);
        } catch (Exception e) {
            log.error("获取任务结果失败", e);
        }

        // 查看线程池状态
        log.info("线程池状态: {}", getPoolStatus());
    }
}