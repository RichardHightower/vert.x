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

package io.advantageous.conekt.net;

import io.advantageous.conekt.AsyncResult;
import io.advantageous.conekt.Handler;
import io.advantageous.conekt.metrics.Measured;
import io.advantageous.conekt.streams.ReadStream;

/**
 * Represents a TCP server
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface NetServer extends Measured {

    /**
     * Return the connect stream for this server. The server can only have at most one handler at any one time.
     * As the server accepts TCP or SSL connections it creates an instance of {@link NetSocket} and passes it to the
     * connect stream {@link ReadStream#handler(Handler)}.
     *
     * @return the connect stream
     */
    NetSocketStream connectStream();

    /**
     * Supply a connect handler for this server. The server can only have at most one connect handler at any one time.
     * As the server accepts TCP or SSL connections it creates an instance of {@link NetSocket} and passes it to the
     * connect handler.
     *
     * @return a reference to this, so the API can be used fluently
     */
    NetServer connectHandler(Handler<NetSocket> handler);

    Handler<NetSocket> connectHandler();

    /**
     * Start listening on the port and host as configured in the {@link NetServerOptions} used when
     * creating the server.
     * <p>
     * The server may not be listening until some time after the call to listen has returned.
     *
     * @return a reference to this, so the API can be used fluently
     */
    NetServer listen();

    /**
     * Like {@link #listen} but providing a handler that will be notified when the server is listening, or fails.
     *
     * @param listenHandler handler that will be notified when listening or failed
     * @return a reference to this, so the API can be used fluently
     */
    NetServer listen(Handler<AsyncResult<NetServer>> listenHandler);

    /**
     * Start listening on the specified port and host, ignoring post and host configured in the {@link NetServerOptions} used when
     * creating the server.
     * <p>
     * Port {@code 0} can be specified meaning "choose an random port".
     * <p>
     * Host {@code 0.0.0.0} can be specified meaning "listen on all available interfaces".
     * <p>
     * The server may not be listening until some time after the call to listen has returned.
     *
     * @return a reference to this, so the API can be used fluently
     */
    NetServer listen(int port, String host);

    /**
     * Like {@link #listen(int, String)} but providing a handler that will be notified when the server is listening, or fails.
     *
     * @param port          the port to listen on
     * @param host          the host to listen on
     * @param listenHandler handler that will be notified when listening or failed
     * @return a reference to this, so the API can be used fluently
     */
    NetServer listen(int port, String host, Handler<AsyncResult<NetServer>> listenHandler);

    /**
     * Start listening on the specified port and host "0.0.0.0", ignoring post and host configured in the
     * {@link NetServerOptions} used when creating the server.
     * <p>
     * Port {@code 0} can be specified meaning "choose an random port".
     * <p>
     * The server may not be listening until some time after the call to listen has returned.
     *
     * @return a reference to this, so the API can be used fluently
     */
    NetServer listen(int port);

    /**
     * Like {@link #listen(int)} but providing a handler that will be notified when the server is listening, or fails.
     *
     * @param port          the port to listen on
     * @param listenHandler handler that will be notified when listening or failed
     * @return a reference to this, so the API can be used fluently
     */
    NetServer listen(int port, Handler<AsyncResult<NetServer>> listenHandler);

    /**
     * Close the server. This will close any currently open connections. The close may not complete until after this
     * method has returned.
     */
    void close();

    /**
     * Like {@link #close} but supplying a handler that will be notified when close is complete.
     *
     * @param completionHandler the handler
     */
    void close(Handler<AsyncResult<Void>> completionHandler);

    /**
     * The actual port the server is listening on. This is useful if you bound the server specifying 0 as port number
     * signifying an ephemeral port
     *
     * @return the actual port the server is listening on.
     */
    int actualPort();
}
