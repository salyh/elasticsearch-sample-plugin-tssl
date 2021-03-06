/*
Copyright 2015 Hendrik Saly

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package de.saly.es.example.tssl.netty;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.netty.channel.ChannelFuture;
import org.elasticsearch.common.netty.channel.ChannelFutureListener;
import org.elasticsearch.common.netty.channel.ChannelHandlerContext;
import org.elasticsearch.common.netty.channel.ChannelStateEvent;
import org.elasticsearch.common.netty.handler.ssl.SslHandler;
import org.elasticsearch.transport.netty.MessageChannelHandler;
import org.elasticsearch.transport.netty.NettyTransport;

public class SecureMessageChannelHandler extends MessageChannelHandler {

    public SecureMessageChannelHandler(final NettyTransport transport, final ESLogger logger) {
        super(transport, logger, "default");
    }

    @Override
    public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) {
        //prevent javax.net.ssl.SSLException: Received close_notify during handshake
        final SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);
        final ChannelFuture handshakeFuture = sslHandler.handshake();
        handshakeFuture.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(final ChannelFuture future) throws Exception {
                if (logger.isTraceEnabled()) {
                    logger.trace("Node to Node encryption cipher is {}/{}", sslHandler.getEngine().getSession().getProtocol(), sslHandler
                            .getEngine().getSession().getCipherSuite());
                }
                ctx.sendUpstream(e);
            }
        });
    }

}
