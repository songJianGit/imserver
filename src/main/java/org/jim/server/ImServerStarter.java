/**
 *
 */
package org.jim.server;

import org.jim.common.ImConfig;
import org.jim.common.ImConst;
import org.jim.server.handler.ImServerAioHandler;
import org.jim.server.helper.redis.RedisMessageHelper;
import org.jim.server.listener.ImGroupListener;
import org.jim.server.listener.ImServerAioListener;
import org.tio.core.intf.GroupListener;
import org.tio.core.ssl.SslConfig;
import org.tio.server.TioServer;

import java.io.IOException;

/**
 *
 * @author WChao
 *
 */
public class ImServerStarter {

    private ImServerAioHandler imAioHandler = null;
    private ImServerAioListener imAioListener = null;
    private ImServerGroupContext imServerGroupContext = null;
    private ImGroupListener imGroupListener = null;
    private TioServer tioServer = null;
    private ImConfig imConfig = null;

    public ImServerStarter(ImConfig imConfig) {
        this(imConfig, null);
    }

    public ImServerStarter(ImConfig imConfig, ImServerAioListener imAioListener) {
        this.imConfig = imConfig;
        this.imAioListener = imAioListener;
        init();
    }

    public void init() {
        System.setProperty("tio.default.read.buffer.size", String.valueOf(imConfig.getReadBufferSize()));
        imAioHandler = new ImServerAioHandler(imConfig);
        if (imAioListener == null) {
            imAioListener = new ImServerAioListener(imConfig);
        }
        GroupListener groupListener = imConfig.getImGroupListener();
        if (groupListener == null) {
            imConfig.setImGroupListener(new ImGroupListener());
        }
        this.imGroupListener = (ImGroupListener) imConfig.getImGroupListener();
        imServerGroupContext = new ImServerGroupContext(imConfig, imAioHandler, imAioListener);
        imServerGroupContext.setGroupListener(imGroupListener);
        if (imConfig.getMessageHelper() == null) {
            imConfig.setMessageHelper(new RedisMessageHelper(imConfig));
        }
        //开启SSL
        if (ImConst.ON.equals(imConfig.getIsSSL())) {
            SslConfig sslConfig = imConfig.getSslConfig();
            if (sslConfig != null) {
                imServerGroupContext.setSslConfig(sslConfig);
            }
        }
        tioServer = new TioServer(imServerGroupContext);
    }

    public void start() throws IOException {
        tioServer.start(this.imConfig.getBindIp(), this.imConfig.getBindPort());
    }

    public void stop() {
        tioServer.stop();
    }
}
