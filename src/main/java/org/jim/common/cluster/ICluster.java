/**
 *
 */
package org.jim.common.cluster;

import org.jim.common.ImPacket;
import org.tio.core.TioConfig;

/**
 *
 * @author WChao
 *
 */
public interface ICluster {
    void clusterToUser(TioConfig tioConfig, String userid, ImPacket packet);

    void clusterToGroup(TioConfig tioConfig, String group, ImPacket packet);

    void clusterToIp(TioConfig tioConfig, String ip, ImPacket packet);

    void clusterToChannelId(TioConfig tioConfig, String channelId, ImPacket packet);
}
