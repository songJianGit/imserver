/**
 *
 */
package org.jim.common.cluster.redis;

import org.apache.commons.lang3.StringUtils;
import org.jim.common.ImAio;
import org.jim.common.ImPacket;
import org.jim.common.cluster.ImClusterConfig;
import org.jim.common.cluster.ImClusterVo;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Tio;
import org.tio.core.TioConfig;
import org.tio.utils.json.Json;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author WChao
 *
 */
public class RedisClusterConfig extends ImClusterConfig {

    public static final String IM_CLUSTER_TOPIC = "JIM_CLUSTER";
    /**
     * 收到了多少次topic
     */
    public static final AtomicLong RECEIVED_TOPIC_COUNT = new AtomicLong();
    private static Logger log = LoggerFactory.getLogger(RedisClusterConfig.class);
    public RTopic<ImClusterVo> rtopic;
    private String topicSuffix;
    private String topic;
    private RedissonClient redisson;

    private RedisClusterConfig(String topicSuffix, RedissonClient redisson, TioConfig tioConfig) {
        this.setTopicSuffix(topicSuffix);
        this.setRedisson(redisson);
        this.tioConfig = tioConfig;
    }

    /**
     * J-IM内置的集群是用redis的topic来实现的，所以不同groupContext就要有一个不同的topicSuffix
     * @param topicSuffix 不同类型的groupContext就要有一个不同的topicSuffix
     * @param redisson
     * @param tioConfig
     * @return
     * @author: WChao
     */
    public static RedisClusterConfig newInstance(String topicSuffix, RedissonClient redisson, TioConfig tioConfig) {
        if (redisson == null) {
            throw new RuntimeException(RedissonClient.class.getSimpleName() + "不允许为空");
        }
        if (tioConfig == null) {
            throw new RuntimeException("GroupContext不允许为空");
        }

        RedisClusterConfig me = new RedisClusterConfig(topicSuffix, redisson, tioConfig);
        me.rtopic = redisson.getTopic(me.topic);
        me.rtopic.addListener(new MessageListener<ImClusterVo>() {
            @Override
            public void onMessage(String channel, ImClusterVo imClusterVo) {
                log.info("收到topic:{}, count:{}, ImClusterVo:{}", channel, RECEIVED_TOPIC_COUNT.incrementAndGet(), Json.toJson(imClusterVo));
                String clientid = imClusterVo.getClientId();
                if (StringUtils.isBlank(clientid)) {
                    log.error("clientid is null");
                    return;
                }
                if (Objects.equals(ImClusterVo.CLIENTID, clientid)) {
                    log.info("自己发布的消息，忽略掉,{}", clientid);
                    return;
                }

                ImPacket packet = imClusterVo.getPacket();
                if (packet == null) {
                    log.error("packet is null");
                    return;
                }
                packet.setFromCluster(true);

                //发送给所有
                boolean isToAll = imClusterVo.isToAll();
                if (isToAll) {
                    //								for (TioConfig tioConfig : me.groupContext) {
                    Tio.sendToAll(tioConfig, packet);
                    //								}
                    //return;
                }

                //发送给指定组
                String group = imClusterVo.getGroup();
                if (StringUtils.isNotBlank(group)) {
                    ImAio.sendToGroup(group, packet);
                    //return;
                }

                //发送给指定用户
                String userid = imClusterVo.getUserid();
                if (StringUtils.isNotBlank(userid)) {
                    //								for (TioConfig tioConfig : me.groupContext) {
                    ImAio.sendToUser(userid, packet);
                    //								}
                    //return;
                }

                //发送给指定token
                String token = imClusterVo.getToken();
                if (StringUtils.isNotBlank(token)) {
                    //								for (TioConfig tioConfig : me.groupContext) {
                    Tio.sendToToken(me.tioConfig, token, packet);
                    //								}
                    //return;
                }

                //发送给指定ip
                String ip = imClusterVo.getIp();
                if (StringUtils.isNotBlank(ip)) {
                    //								for (TioConfig tioConfig : me.groupContext) {
                    ImAio.sendToIp(me.tioConfig, ip, packet);
                    //								}
                    //return;
                }
            }
        });
        return me;
    }

    public String getTopicSuffix() {
        return topicSuffix;
    }

    public void setTopicSuffix(String topicSuffix) {
        this.topicSuffix = topicSuffix;
        this.topic = topicSuffix + IM_CLUSTER_TOPIC;

    }

    public String getTopic() {
        return topic;
    }

    public void publishAsyn(ImClusterVo imClusterVo) {
        rtopic.publishAsync(imClusterVo);
    }

    public void publish(ImClusterVo imClusterVo) {
        rtopic.publish(imClusterVo);
    }

    public RedissonClient getRedisson() {
        return redisson;
    }

    public void setRedisson(RedissonClient redisson) {
        this.redisson = redisson;
    }

    @Override
    public void send(ImClusterVo imClusterVo) {
        rtopic.publish(imClusterVo);
    }

    @Override
    public void sendAsyn(ImClusterVo imClusterVo) {
        rtopic.publishAsync(imClusterVo);
    }
}
