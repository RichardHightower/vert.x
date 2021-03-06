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

package io.advantageous.conekt.net.impl;

import io.advantageous.conekt.impl.ContextImpl;
import io.netty.channel.EventLoop;
import io.advantageous.conekt.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class HandlerManager<T> {

    private static final Logger log = LoggerFactory.getLogger(HandlerManager.class);

    private final ConektEventLoopGroup availableWorkers;
    private final ConcurrentMap<EventLoop, Handlers<T>> handlerMap = new ConcurrentHashMap<>();

    // We maintain a separate hasHandlers variable so we can implement hasHandlers() efficiently
    // As it is called for every HTTP message received
    private volatile boolean hasHandlers;

    public HandlerManager(ConektEventLoopGroup availableWorkers) {
        this.availableWorkers = availableWorkers;
    }

    public boolean hasHandlers() {
        return hasHandlers;
    }

    public HandlerHolder<T> chooseHandler(EventLoop worker) {
        Handlers<T> handlers = handlerMap.get(worker);
        return handlers == null ? null : handlers.chooseHandler();
    }

    public synchronized void addHandler(Handler<T> handler, ContextImpl context) {
        EventLoop worker = context.nettyEventLoop();
        availableWorkers.addWorker(worker);
        Handlers<T> handlers = new Handlers<>();
        Handlers<T> prev = handlerMap.putIfAbsent(worker, handlers);
        if (prev != null) {
            handlers = prev;
        }
        handlers.addHandler(new HandlerHolder<>(context, handler));
        hasHandlers = true;
    }

    public synchronized void removeHandler(Handler<T> handler, ContextImpl context) {
        EventLoop worker = context.nettyEventLoop();
        Handlers<T> handlers = handlerMap.get(worker);
        if (!handlers.removeHandler(new HandlerHolder<>(context, handler))) {
            throw new IllegalStateException("Can't find handler");
        }
        if (handlers.isEmpty()) {
            handlerMap.remove(worker);
        }
        if (handlerMap.isEmpty()) {
            hasHandlers = false;
        }
        //Available workers does it's own reference counting -since workers can be shared across different Handlers
        availableWorkers.removeWorker(worker);
    }

    private static final class Handlers<T> {
        private final List<HandlerHolder<T>> list = new CopyOnWriteArrayList<>();
        private int pos;

        HandlerHolder<T> chooseHandler() {
            HandlerHolder<T> handler = list.get(pos);
            pos++;
            checkPos();
            return handler;
        }

        void addHandler(HandlerHolder<T> handler) {
            list.add(handler);
        }

        boolean removeHandler(HandlerHolder<T> handler) {
            if (list.remove(handler)) {
                checkPos();
                return true;
            } else {
                return false;
            }
        }

        boolean isEmpty() {
            return list.isEmpty();
        }

        void checkPos() {
            if (pos == list.size()) {
                pos = 0;
            }
        }
    }

}
