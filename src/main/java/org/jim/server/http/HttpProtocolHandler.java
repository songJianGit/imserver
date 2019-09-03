package org.jim.server.http;

import org.jim.common.ImConfig;
import org.jim.common.ImConst;
import org.jim.common.http.*;
import org.jim.common.http.handler.IHttpRequestHandler;
import org.jim.common.protocol.IProtocol;
import org.jim.common.session.id.impl.UUIDSessionIdGenerator;
import org.jim.server.ImServerStarter;
import org.jim.server.handler.AbstractProtocolHandler;
import org.jim.server.http.mvc.Routes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.TioConfig;
import org.tio.core.exception.AioDecodeException;
import org.tio.core.intf.Packet;
import org.tio.utils.SystemTimer;
import org.tio.utils.cache.guava.GuavaCache;

import java.nio.ByteBuffer;

/**
 * 版本: [1.0]
 * 功能说明:
 * 作者: WChao 创建时间: 2017年8月3日 下午3:07:54
 */
public class HttpProtocolHandler extends AbstractProtocolHandler {

    private Logger log = LoggerFactory.getLogger(HttpProtocolHandler.class);

    private HttpConfig httpConfig;

    private IHttpRequestHandler httpRequestHandler;

    public HttpProtocolHandler() {
    }

    public HttpProtocolHandler(HttpConfig httpServerConfig) {
        this.httpConfig = httpServerConfig;
    }

    @Override
    public void init(ImConfig imConfig) throws Exception {
        long start = SystemTimer.currentTimeMillis();
        this.httpConfig = imConfig.getHttpConfig();
        if (httpConfig.getSessionStore() == null) {
            GuavaCache guavaCache = GuavaCache.register(httpConfig.getSessionCacheName(), null, httpConfig.getSessionTimeout());
            httpConfig.setSessionStore(guavaCache);
        }
        if (httpConfig.getPageRoot() == null) {
            httpConfig.setPageRoot("page");
        }
        if (httpConfig.getSessionIdGenerator() == null) {
            httpConfig.setSessionIdGenerator(UUIDSessionIdGenerator.instance);
        }
        if (httpConfig.getScanPackages() == null) {
            //J-IM MVC需要扫描的根目录包
            String[] scanPackages = new String[]{ImServerStarter.class.getPackage().getName()};
            httpConfig.setScanPackages(scanPackages);
        } else {
            String[] scanPackages = new String[httpConfig.getScanPackages().length + 1];
            scanPackages[0] = ImServerStarter.class.getPackage().getName();
            System.arraycopy(httpConfig.getScanPackages(), 0, scanPackages, 1, httpConfig.getScanPackages().length);
            httpConfig.setScanPackages(scanPackages);
        }
        Routes routes = new Routes(httpConfig.getScanPackages());
        httpRequestHandler = new DefaultHttpRequestHandler(httpConfig, routes);
        httpConfig.setHttpRequestHandler(httpRequestHandler);
        imConfig.getTioConfig().setAttribute(GroupContextKey.HTTP_SERVER_CONFIG, httpConfig);
        long end = SystemTimer.currentTimeMillis();
        long iv = end - start;
        log.info("J-IM Http Server初始化完毕,耗时:{}ms", iv);
    }

    @Override
    public ByteBuffer encode(Packet packet, TioConfig tioConfig, ChannelContext channelContext) {
        HttpResponse httpResponsePacket = (HttpResponse) packet;
        ByteBuffer byteBuffer = HttpResponseEncoder.encode(httpResponsePacket, tioConfig, channelContext, false);
        return byteBuffer;
    }

    @Override
    public void handler(Packet packet, ChannelContext channelContext) throws Exception {
        HttpRequest httpRequestPacket = (HttpRequest) packet;
        HttpResponse httpResponsePacket = httpRequestHandler.handler(httpRequestPacket, httpRequestPacket.getRequestLine());
        Tio.send(channelContext, httpResponsePacket);
    }

    @Override
    public Packet decode(ByteBuffer buffer, ChannelContext channelContext) throws AioDecodeException {
        HttpRequest request = HttpRequestDecoder.decode(buffer, channelContext, true);
        channelContext.setAttribute(ImConst.HTTP_REQUEST, request);
        return request;
    }

    public IHttpRequestHandler getHttpRequestHandler() {
        return httpRequestHandler;
    }

    public void setHttpRequestHandler(IHttpRequestHandler httpRequestHandler) {
        this.httpRequestHandler = httpRequestHandler;
    }

    public HttpConfig getHttpConfig() {
        return httpConfig;
    }

    public void setHttpConfig(HttpConfig httpConfig) {
        this.httpConfig = httpConfig;
    }

    @Override
    public IProtocol protocol() {
        return new HttpProtocol();
    }
}
