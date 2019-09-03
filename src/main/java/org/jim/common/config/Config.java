/**
 *
 */
package org.jim.common.config;

import org.jim.common.cluster.ImCluster;
import org.jim.common.message.MessageHelper;
import org.tio.core.TioConfig;
import org.tio.core.intf.GroupListener;
import org.tio.core.ssl.SslConfig;

/**
 * @author WChao
 * 2018/08/26
 */
public class Config {
    /**
     * IP地址
     */
    protected String bindIp = null;
    /**
     * 监听端口
     */
    protected Integer bindPort = 80;
    /**
     * 心跳包发送时长heartbeatTimeout/2
     */
    protected long heartbeatTimeout = 0;

    /**
     * 全局群组上下文;
     */
    protected TioConfig tioConfig;
    /**
     * 群组监听器;
     */
    protected GroupListener imGroupListener;
    /**
     * 用户消息持久化助手;
     */
    protected MessageHelper messageHelper;
    /**
     * 是否开启持久化;
     */
    protected String isStore = "off";
    /**
     * 是否开启集群;
     */
    protected String isCluster = "off";
    /**
     * 是否开启SSL加密
     */
    protected String isSSL = "off";
    /**
     * SSL配置
     */
    protected SslConfig sslConfig;
    /**
     * 集群配置
     * 如果此值不为null，就表示要集群
     */
    protected ImCluster cluster;
    /**
     *  默认的接收数据的buffer size
     */
    protected long readBufferSize = 1024 * 2;

    public String getBindIp() {
        return bindIp;
    }

    public void setBindIp(String bindIp) {
        this.bindIp = bindIp;
    }

    public Integer getBindPort() {
        return bindPort;
    }

    public void setBindPort(Integer bindPort) {
        this.bindPort = bindPort;
    }

    public long getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    public void setHeartbeatTimeout(long heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }

    public TioConfig getTioConfig() {
        return tioConfig;
    }

    public void setTioConfig(TioConfig tioConfig) {
        this.tioConfig = tioConfig;
    }

    public GroupListener getImGroupListener() {
        return imGroupListener;
    }

    public void setImGroupListener(GroupListener imGroupListener) {
        this.imGroupListener = imGroupListener;
    }

    public MessageHelper getMessageHelper() {
        return messageHelper;
    }

    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }

    public String getIsStore() {
        return isStore;
    }

    public void setIsStore(String isStore) {
        this.isStore = isStore;
    }

    public String getIsCluster() {
        return isCluster;
    }

    public void setIsCluster(String isCluster) {
        this.isCluster = isCluster;
    }

    public String getIsSSL() {
        return isSSL;
    }

    public void setIsSSL(String isSSL) {
        this.isSSL = isSSL;
    }

    public SslConfig getSslConfig() {
        return sslConfig;
    }

    public void setSslConfig(SslConfig sslConfig) {
        this.sslConfig = sslConfig;
    }

    public ImCluster getCluster() {
        return cluster;
    }

    public void setCluster(ImCluster cluster) {
        this.cluster = cluster;
    }

    public long getReadBufferSize() {
        return readBufferSize;
    }

    public void setReadBufferSize(long readBufferSize) {
        this.readBufferSize = readBufferSize;
    }

    public interface Builder {
        /**
         * 配置构建接口
         * @return
         * @throws Exception
         */
        Config build() throws Exception;
    }
}
