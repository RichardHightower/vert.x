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

package io.advantageous.conekt.streams;

import io.advantageous.conekt.Handler;

/**
 * Represents a stream of data that can be written to.
 * <p>
 * Any class that implements this interface can be used by a {@link Pump} to pump data from a {@code ReadStream}
 * to it.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface WriteStream<T> extends StreamBase {

    /**
     * Set an exception handler on the write stream.
     *
     * @param handler the exception handler
     * @return a reference to this, so the API can be used fluently
     */
    @Override
    WriteStream<T> exceptionHandler(Handler<Throwable> handler);

    /**
     * Write some data to the stream. The data is put on an internal write queue, and the write actually happens
     * asynchronously. To avoid running out of memory by putting too much on the write queue,
     * check the {@link #writeQueueFull} method before writing. This is done automatically if using a {@link Pump}.
     *
     * @param data the data to write
     * @return a reference to this, so the API can be used fluently
     */
    WriteStream<T> write(T data);

    /**
     * Ends the stream.
     * <p>
     * Once the stream has ended, it cannot be used any more.
     */
    void end();

    /**
     * Same as {@link #end()} but writes some data to the stream before ending.
     */
    default void end(T t) {
        write(t);
        end();
    }

    /**
     * Set the maximum size of the write queue to {@code maxSize}. You will still be able to write to the stream even
     * if there is more than {@code maxSize} bytes in the write queue. This is used as an indicator by classes such as
     * {@code Pump} to provide flow control.
     *
     * @param maxSize the max size of the write stream
     * @return a reference to this, so the API can be used fluently
     */
    WriteStream<T> setWriteQueueMaxSize(int maxSize);

    /**
     * This will return {@code true} if there are more bytes in the write queue than the value set using {@link
     * #setWriteQueueMaxSize}
     *
     * @return true if write queue is full
     */
    boolean writeQueueFull();

    /**
     * Set a drain handler on the stream. If the write queue is full, then the handler will be called when the write
     * queue has been reduced to maxSize / 2. See {@link Pump} for an example of this being used.
     *
     * @param handler the handler
     * @return a reference to this, so the API can be used fluently
     */
    WriteStream<T> drainHandler(Handler<Void> handler);

}
