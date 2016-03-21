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

/**
 * == Streams
 * <p>
 * There are several objects in Vert.x that allow items to be read from and written.
 * <p>
 * In previous versions the {@link io.vertx.core.streams} package was manipulating {@link io.smallvertx.core.buffer.Buffer}
 * objects exclusively. From now, streams are not coupled to buffers anymore and they work with any kind of objects.
 * <p>
 * In Vert.x, write calls return immediately, and writes are queued internally.
 * <p>
 * It's not hard to see that if you write to an object faster than it can actually write the data to
 * its underlying resource, then the write queue can grow unbounded - eventually resulting in
 * memory exhaustion.
 * <p>
 * To solve this problem a simple flow control (_back-pressure_) capability is provided by some objects in the Vert.x API.
 * <p>
 * Any flow control aware object that can be _written-to_ implements {@link io.smallvertx.core.streams.WriteStream},
 * while any flow control object that can be _read-from_ is said to implement {@link io.smallvertx.core.streams.ReadStream}.
 * <p>
 * Let's take an example where we want to read from a `ReadStream` then write the data to a `WriteStream`.
 * <p>
 * A very simple example would be reading from a {@link io.smallvertx.core.net.NetSocket} then writing back to the
 * same `NetSocket` - since `NetSocket` implements both `ReadStream` and `WriteStream`. Note that this works
 * between any `ReadStream` and `WriteStream` compliant object, including HTTP requests, HTTP responses,
 * async files I/O, WebSockets, etc.
 * <p>
 * A naive way to do this would be to directly take the data that has been read and immediately write it
 * to the `NetSocket`:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.StreamsExamples#pump1(Vertx)}
 * ----
 * <p>
 * There is a problem with the example above: if data is read from the socket faster than it can be
 * written back to the socket, it will build up in the write queue of the `NetSocket`, eventually
 * running out of RAM. This might happen, for example if the client at the other end of the socket
 * wasn't reading fast enough, effectively putting back-pressure on the connection.
 * <p>
 * Since `NetSocket` implements `WriteStream`, we can check if the `WriteStream` is full before
 * writing to it:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.StreamsExamples#pump2(Vertx)}
 * ----
 * <p>
 * This example won't run out of RAM but we'll end up losing data if the write queue gets full. What we
 * really want to do is pause the `NetSocket` when the write queue is full:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.StreamsExamples#pump3(Vertx)}
 * ----
 * <p>
 * We're almost there, but not quite. The `NetSocket` now gets paused when the file is full, but we also need to unpause
 * it when the write queue has processed its backlog:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.StreamsExamples#pump4(Vertx)}
 * ----
 * <p>
 * And there we have it. The {@link io.smallvertx.core.streams.WriteStream#drainHandler} event handler will
 * get called when the write queue is ready to accept more data, this resumes the `NetSocket` that
 * allows more data to be read.
 * <p>
 * Wanting to do this is quite common while writing Vert.x applications, so we provide a helper class
 * called {@link io.smallvertx.core.streams.Pump} that does all of this hard work for you.
 * You just feed it the `ReadStream` plus the `WriteStream` then start it:
 * <p>
 * [source,$lang]
 * ----
 * {@link examples.StreamsExamples#pump5(Vertx)}
 * ----
 * <p>
 * This does exactly the same thing as the more verbose example.
 * <p>
 * Let's now look at the methods on `ReadStream` and `WriteStream` in more detail:
 * <p>
 * === ReadStream
 * <p>
 * `ReadStream` is implemented by {@link io.smallvertx.core.http.HttpClientResponse}, {@link io.smallvertx.core.datagram.DatagramSocket},
 * {@link io.smallvertx.core.http.HttpClientRequest}, {@link io.smallvertx.core.http.HttpServerFileUpload},
 * {@link io.smallvertx.core.http.HttpServerRequest}, {@link io.smallvertx.core.http.HttpServerRequestStream},
 * {@link io.smallvertx.core.eventbus.MessageConsumer}, {@link io.smallvertx.core.net.NetSocket}, {@link io.smallvertx.core.net.NetSocketStream},
 * {@link io.smallvertx.core.http.WebSocket}, {@link io.smallvertx.core.http.WebSocketStream}, {@link io.smallvertx.core.TimeoutStream},
 * {@link io.smallvertx.core.file.AsyncFile}.
 * <p>
 * Functions:
 * <p>
 * - {@link io.smallvertx.core.streams.ReadStream#handler}:
 * set a handler which will receive items from the ReadStream.
 * - {@link io.smallvertx.core.streams.ReadStream#pause}:
 * pause the handler. When paused no items will be received in the handler.
 * - {@link io.smallvertx.core.streams.ReadStream#resume}:
 * resume the handler. The handler will be called if any item arrives.
 * - {@link io.smallvertx.core.streams.ReadStream#exceptionHandler}:
 * Will be called if an exception occurs on the ReadStream.
 * - {@link io.smallvertx.core.streams.ReadStream#endHandler}:
 * Will be called when end of stream is reached. This might be when EOF is reached if the ReadStream represents a file,
 * or when end of request is reached if it's an HTTP request, or when the connection is closed if it's a TCP socket.
 * <p>
 * === WriteStream
 * <p>
 * `WriteStream` is implemented by {@link io.smallvertx.core.http.HttpClientRequest}, {@link io.smallvertx.core.http.HttpServerResponse}
 * {@link io.smallvertx.core.http.WebSocket}, {@link io.smallvertx.core.net.NetSocket}, {@link io.smallvertx.core.file.AsyncFile},
 * {@link io.smallvertx.core.datagram.PacketWritestream} and {@link io.smallvertx.core.eventbus.MessageProducer}
 * <p>
 * Functions:
 * <p>
 * - {@link io.smallvertx.core.streams.WriteStream#write}:
 * write an object to the WriteStream. This method will never block. Writes are queued internally and asynchronously
 * written to the underlying resource.
 * - {@link io.smallvertx.core.streams.WriteStream#setWriteQueueMaxSize}:
 * set the number of object at which the write queue is considered _full_, and the method {@link io.smallvertx.core.streams.WriteStream#writeQueueFull()}
 * returns `true`. Note that, when the write queue is considered full, if write is called the data will still be accepted
 * and queued. The actual number depends on the stream implementation, for {@link io.smallvertx.core.buffer.Buffer} the size
 * represents the actual number of bytes written and not the number of buffers.
 * - {@link io.smallvertx.core.streams.WriteStream#writeQueueFull}:
 * returns `true` if the write queue is considered full.
 * - {@link io.smallvertx.core.streams.WriteStream#exceptionHandler}:
 * Will be called if an exception occurs on the `WriteStream`.
 * - {@link io.smallvertx.core.streams.WriteStream#drainHandler}:
 * The handler will be called if the `WriteStream` is considered no longer full.
 * <p>
 * === Pump
 * <p>
 * Instances of Pump have the following methods:
 * <p>
 * - {@link io.smallvertx.core.streams.Pump#start}:
 * Start the pump.
 * - {@link io.smallvertx.core.streams.Pump#stop}:
 * Stops the pump. When the pump starts it is in stopped mode.
 * - {@link io.smallvertx.core.streams.Pump#setWriteQueueMaxSize}:
 * This has the same meaning as {@link io.smallvertx.core.streams.WriteStream#setWriteQueueMaxSize} on the `WriteStream`.
 * <p>
 * A pump can be started and stopped multiple times.
 * <p>
 * When a pump is first created it is _not_ started. You need to call the `start()` method to start it.
 */
package io.smallvertx.core.streams;

import io.smallvertx.core.Vertx;