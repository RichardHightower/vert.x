/*
 * Copyright (c) 2011-2013 The original author or authors
 *  ------------------------------------------------------
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *      The Eclipse Public License is available at
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.smallvertx.core.datagram.impl;

import io.smallvertx.core.AsyncResult;
import io.smallvertx.core.Handler;
import io.smallvertx.core.buffer.Buffer;
import io.smallvertx.core.datagram.DatagramSocket;
import io.smallvertx.core.datagram.PacketWritestream;

/**
 * A write stream for packets.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class PacketWriteStreamImpl implements PacketWritestream, Handler<AsyncResult<DatagramSocket>> {

    private final int port;
    private final String host;
    private DatagramSocketImpl datagramSocket;
    private Handler<Throwable> exceptionHandler;

    PacketWriteStreamImpl(DatagramSocketImpl datagramSocket, int port, String host) {
        this.datagramSocket = datagramSocket;
        this.port = port;
        this.host = host;
    }

    @Override
    public void handle(AsyncResult<DatagramSocket> event) {
        if (event.failed() && exceptionHandler != null) {
            exceptionHandler.handle(event.cause());
        }
    }

    @Override
    public PacketWritestream exceptionHandler(Handler<Throwable> handler) {
        exceptionHandler = handler;
        return this;
    }

    @Override
    public PacketWritestream write(Buffer data) {
        datagramSocket.send(data, port, host, this);
        return this;
    }

    @Override
    public PacketWritestream setWriteQueueMaxSize(int maxSize) {
        return this;
    }

    @Override
    public boolean writeQueueFull() {
        return false;
    }

    @Override
    public PacketWritestream drainHandler(Handler<Void> handler) {
        return this;
    }

    @Override
    public void end() {
    }
}