/*
 *
 *  * Copyright (c) 2011-2016 The original author or authors
 *  * This project contains modified work from the Vert.x Project.
 *  * The Vert.x project Copyright is owned by Red Hat and/or the
 *  * original authors of the Vert.x project including Tim Fox, Julien Vet,
 *  * Norman Maurer, and many others.
 *  * We have left the original author tags on this MODIFIED COPY/FORK.
 *  *
 *  * Modified work is Copyright (c) 2015-2016 Rick Hightower and Geoff Chandler.
 *  * ------------------------------------------------------
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the Eclipse Public License v1.0
 *  * and Apache License v2.0 which accompanies this distribution.
 *  *
 *  *     The Eclipse Public License is available at
 *  *     http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  *     The Apache License v2.0 is available at
 *  *     http://www.opensource.org/licenses/apache2.0.php
 *  *
 *  * You may elect to redistribute this code under either of these licenses.
 *
 */
package io.advantageous.conekt.datagram.impl;

import io.advantageous.conekt.buffer.Buffer;
import io.advantageous.conekt.impl.ContextImpl;
import io.advantageous.conekt.net.impl.ConektHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

/**
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
final class DatagramServerHandler extends ConektHandler<DatagramSocketImpl> {

    private final DatagramSocketImpl socket;

    DatagramServerHandler(DatagramSocketImpl socket) {
        this.socket = socket;
    }

    @Override
    protected DatagramSocketImpl getConnection(Channel channel) {
        return socket;
    }

    @Override
    protected DatagramSocketImpl removeConnection(Channel channel) {
        return socket;
    }


    @SuppressWarnings("unchecked")
    @Override
    protected void channelRead(final DatagramSocketImpl server, final ContextImpl context, ChannelHandlerContext chctx, final Object msg) throws Exception {
        context.executeFromIO(() -> server.handlePacket((io.advantageous.conekt.datagram.DatagramPacket) msg));
    }

    @Override
    protected Object safeObject(Object msg, ByteBufAllocator allocator) throws Exception {
        if (msg instanceof DatagramPacket) {
            DatagramPacket packet = (DatagramPacket) msg;
            ByteBuf content = packet.content();
            if (content.isDirect()) {
                content = safeBuffer(content, allocator);
            }
            return new DatagramPacketImpl(packet.sender(), Buffer.buffer(content));
        }
        return msg;
    }
}