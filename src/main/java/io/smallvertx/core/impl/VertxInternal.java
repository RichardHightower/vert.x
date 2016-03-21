/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.smallvertx.core.impl;


import io.netty.channel.EventLoopGroup;
import io.smallvertx.core.AsyncResult;
import io.smallvertx.core.Vertx;
import io.smallvertx.core.http.impl.HttpServerImpl;
import io.smallvertx.core.net.impl.ServerID;
import io.smallvertx.core.Handler;
import io.smallvertx.core.json.JsonObject;
import io.smallvertx.core.net.impl.NetServerImpl;
import io.smallvertx.core.spi.metrics.VertxMetrics;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * This interface provides services for vert.x core internal use only
 * It is not part of the public API and should not be used by
 * developers creating vert.x applications
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface VertxInternal extends Vertx {

    @Override
    ContextImpl getOrCreateContext();

    EventLoopGroup getEventLoopGroup();

    EventLoopGroup getAcceptorEventLoopGroup();

    ExecutorService getWorkerPool();

    Map<ServerID, HttpServerImpl> sharedHttpServers();

    Map<ServerID, NetServerImpl> sharedNetServers();

    VertxMetrics metricsSPI();

    /**
     * Get the current context
     *
     * @return the context
     */
    ContextImpl getContext();

    /**
     * @return event loop context
     */
    EventLoopContext createEventLoopContext(String deploymentID, JsonObject config, ClassLoader tccl);

    /**
     * @return worker loop context
     */
    ContextImpl createWorkerContext(boolean multiThreaded, String deploymentID, JsonObject config, ClassLoader tccl);


    Deployment getDeployment(String deploymentID);

    File resolveFile(String fileName);

    <T> void executeBlockingInternal(Action<T> action, Handler<AsyncResult<T>> resultHandler);


}