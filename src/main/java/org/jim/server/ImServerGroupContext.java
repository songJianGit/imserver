/**
 *
 */
package org.jim.server;

import org.jim.common.ImAio;
import org.jim.common.ImConfig;
import org.jim.common.ImConst;
import org.jim.common.cache.redis.RedissonTemplate;
import org.jim.common.cluster.redis.RedisCluster;
import org.jim.common.cluster.redis.RedisClusterConfig;
import org.jim.server.command.CommandManager;
import org.jim.server.handler.ImServerAioHandler;
import org.jim.server.handler.ProtocolHandlerManager;
import org.jim.server.listener.ImServerAioListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.server.ServerTioConfig;
import org.tio.utils.Threads;
import org.tio.utils.thread.pool.DefaultThreadFactory;
import org.tio.utils.thread.pool.SynThreadPoolExecutor;

import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author WChao
 *
 */
public class ImServerGroupContext extends ServerTioConfig {

    private static int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    protected SynThreadPoolExecutor timExecutor = null;
    private Logger log = LoggerFactory.getLogger(ImServerGroupContext.class);
    /**
     * IM配置加载类;
     */
    private ImConfig imConfig;

    public ImServerGroupContext(ImConfig imConfig, ImServerAioHandler imServerAioHandler, ImServerAioListener imServerAioListener) {
        super(imServerAioHandler, imServerAioListener);
        this.imConfig = imConfig;
        this.setHeartbeatTimeout(imConfig.getHeartbeatTimeout());
        //是否开启集群
        if (ImConst.ON.equals(imConfig.getIsCluster())) {
            imConfig.setIsStore(ImConst.ON);
            if (imConfig.getCluster() == null) {
                try {
                    RedisCluster redisCluster = new RedisCluster(RedisClusterConfig.newInstance("REDIS_", RedissonTemplate.me().getRedissonClient(), this));
                    imConfig.setCluster(redisCluster);
                } catch (Exception e) {
                    log.error("连接集群配置出现异常,请检查！", e);
                }
            }
        } else {
            imConfig.setCluster(null);
        }
        if (this.timExecutor == null) {
            LinkedBlockingQueue<Runnable> timQueue = new LinkedBlockingQueue<>();
            String timThreadName = ImConst.JIM;
            this.timExecutor = new SynThreadPoolExecutor(CORE_POOL_SIZE, CORE_POOL_SIZE, Threads.KEEP_ALIVE_TIME, timQueue,
                    DefaultThreadFactory.getInstance(timThreadName, Thread.NORM_PRIORITY), timThreadName);
            this.timExecutor.prestartAllCoreThreads();
        }
        imConfig.setTioConfig(this);
        ProtocolHandlerManager.init(imConfig);
        CommandManager.init(imConfig);
        ImAio.imConfig = imConfig;
    }

    public SynThreadPoolExecutor getTimExecutor() {
        return timExecutor;
    }

    public ImConfig getImConfig() {
        return imConfig;
    }

    public void setImConfig(ImConfig imConfig) {
        this.imConfig = imConfig;
    }

}
