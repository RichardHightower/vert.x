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

package io.advantageous.conekt.eventbus;

import io.advantageous.conekt.AsyncResult;
import io.advantageous.conekt.streams.ReadStream;
import io.advantageous.conekt.Handler;

/**
 * An event bus consumer object representing a stream of message to an {@link EventBus} address that can
 * be read from.
 * <p>
 * The {@link EventBus#consumer(String)} or {@link EventBus#localConsumer(String)}
 * creates a new consumer, the returned consumer is not yet registered against the event bus. Registration
 * is effective after the {@link #handler(Handler)} method is invoked.<p>
 * <p>
 * The consumer is unregistered from the event bus using the {@link #unregister()} method or by calling the
 * {@link #handler(Handler)} with a null value..
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public interface MessageConsumer<T> extends ReadStream<Message<T>> {

    @Override
    MessageConsumer<T> exceptionHandler(Handler<Throwable> handler);

    @Override
    MessageConsumer<T> handler(Handler<Message<T>> handler);

    @Override
    MessageConsumer<T> pause();

    @Override
    MessageConsumer<T> resume();

    @Override
    MessageConsumer<T> endHandler(Handler<Void> endHandler);

    /**
     * @return a read stream for the body of the message stream.
     */
    ReadStream<T> bodyStream();

    /**
     * @return true if the current consumer is registered
     */
    boolean isRegistered();

    /**
     * @return The address the handler was registered with.
     */
    String address();

    /**
     * @return the maximum number of messages that can be buffered when this stream is paused
     */
    int getMaxBufferedMessages();

    /**
     * Set the number of messages this registration will buffer when this stream is paused. The default
     * value is <code>0</code>. When a new value is set, buffered messages may be discarded to reach
     * the new value.
     *
     * @param maxBufferedMessages the maximum number of messages that can be buffered
     * @return this registration
     */
    MessageConsumer<T> setMaxBufferedMessages(int maxBufferedMessages);

    /**
     * Optional method which can be called to indicate when the registration has been propagated across the cluster.
     *
     * @param completionHandler the completion handler
     */
    void completionHandler(Handler<AsyncResult<Void>> completionHandler);

    /**
     * Unregisters the handler which created this registration
     */
    void unregister();

    /**
     * Unregisters the handler which created this registration
     *
     * @param completionHandler the handler called when the unregister is done. For example in a cluster when all nodes of the
     *                          event bus have been unregistered.
     */
    void unregister(Handler<AsyncResult<Void>> completionHandler);
}
