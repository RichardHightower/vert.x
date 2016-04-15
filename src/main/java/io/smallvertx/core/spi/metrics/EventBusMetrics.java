/*
 * Copyright (c) 2011-2014 The original author or authors
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

package io.smallvertx.core.spi.metrics;

import io.smallvertx.core.eventbus.ReplyFailure;

/**
 * The event bus metrics SPI which Vert.x will use to call when each event occurs.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public interface EventBusMetrics<H> extends Metrics {

    /**
     * Called when a handler is registered on the event bus.<p/>
     * <p>
     * No specific thread and context can be expected when this method is called.
     *
     * @param address        the address used to register the handler
     * @param repliedAddress null when the handler is not a reply handler, otherwise the address this handler is replying to
     */
    H handlerRegistered(String address, String repliedAddress);

    /**
     * Called when a handler has been unregistered from the event bus.<p/>
     * <p>
     * No specific thread and context can be expected when this method is called.
     *
     * @param handler the unregistered handler
     */
    void handlerUnregistered(H handler);

    /**
     * Called when an handler begin to process a message.<p/>
     * <p>
     * The thread model depends on the actual context thats registered the handler.<p/>
     * <p>
     * <h3>Event loop context</h3>
     * <p>
     * This method is invoked with the handler event loop thread.
     * <p>
     * <h3>Worker context</h3>
     * <p>
     * This method is invoked with a worker thread.
     *
     * @param handler the handler processing the message
     * @param local   when the received message is local
     */
    void beginHandleMessage(H handler, boolean local);

    /**
     * Called when an handler finish to process a message.<p/>
     * <p>
     * The thread model depends on the actual context thats registered the handler.<p/>
     * <p>
     * <h3>Event loop context</h3>
     * <p>
     * This method is invoked with the handler event loop thread.
     * <p>
     * <h3>Worker context</h3>
     * <p>
     * This method is invoked with a worker thread.
     *
     * @param handler the handler processing the message
     * @param failure an optional failure thrown by handler
     */
    void endHandleMessage(H handler, Throwable failure);

    /**
     * Called when a message has been sent or published.<p/>
     * <p>
     * No specific thread and context can be expected when this method is called.
     *
     * @param address the address
     * @param publish true when the message is published
     * @param local   the message is processed locally
     * @param remote  the message is sent on the cluster
     */
    void messageSent(String address, boolean publish, boolean local, boolean remote);

    /**
     * Called when a message is received.<p/>
     * <p>
     * No specific thread and context can be expected when this method is called.
     *
     * @param address  the address
     * @param publish  true when the message is published
     * @param local    true when the message is received locally
     * @param handlers the number of handlers that process the message
     */
    void messageReceived(String address, boolean publish, boolean local, int handlers);

    /**
     * A message has been sent over the network.<p/>
     * <p>
     * No specific thread and context can be expected when this method is called.
     *
     * @param address       the message address
     * @param numberOfBytes the number of bytes written
     */
    void messageWritten(String address, int numberOfBytes);

    /**
     * A message has been received from the network.<p/>
     * <p>
     * No specific thread and context can be expected when this method is called.
     *
     * @param address       the message address
     * @param numberOfBytes the number of bytes read
     */
    void messageRead(String address, int numberOfBytes);

    /**
     * Called whenever the event bus delivers a reply failure to the sender/publisher, the
     * {@link ReplyFailure reply failure} indicates the nature of the failure.<p/>
     * <p>
     * No specific thread and context can be expected when this method is called.
     *
     * @param address the address
     * @param failure the {@link ReplyFailure}
     */
    void replyFailure(String address, ReplyFailure failure);
}
