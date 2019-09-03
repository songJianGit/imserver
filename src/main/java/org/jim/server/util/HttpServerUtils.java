package org.jim.server.util;

import org.jim.common.http.GroupContextKey;
import org.jim.common.http.HttpConfig;
import org.jim.common.http.HttpRequest;
import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;

/**
 * @author WChao
 * 2017年8月18日 下午5:47:00
 */
public class HttpServerUtils {
    /**
     * @author WChao
     */
    public HttpServerUtils() {
    }

    /**
     * @param request
     * @return
     * @author WChao
     */
    public static HttpConfig getHttpConfig(HttpRequest request) {
        ChannelContext channelContext = request.getChannelContext();
        TioConfig tioConfig = channelContext.getTioConfig();
        HttpConfig httpConfig = (HttpConfig) tioConfig.get(GroupContextKey.HTTP_SERVER_CONFIG);
        return httpConfig;
    }

    /**
     * @param args
     * @author WChao
     */
    public static void main(String[] args) {

    }
}
