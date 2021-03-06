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
package io.advantageous.conekt.datagram;

import io.advantageous.conekt.AsyncResult;
import io.advantageous.conekt.streams.ReadStream;
import io.advantageous.conekt.Handler;
import io.advantageous.conekt.buffer.Buffer;
import io.advantageous.conekt.metrics.Measured;
import io.advantageous.conekt.net.SocketAddress;

/**
 * A datagram socket can be used to send {@link DatagramPacket}'s to remote datagram servers
 * and receive {@link DatagramPacket}s .
 * <p>
 * Usually you use a datagram socket to send UDP over the wire. UDP is connection-less which means you are not connected
 * to the remote peer in a persistent way. Because of this you have to supply the address and port of the remote peer
 * when sending data.
 * <p>
 * You can send data to ipv4 or ipv6 addresses, which also include multicast addresses.
 * <p>
 * Please consult the documentation for more information on datagram sockets.
 *
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
public interface DatagramSocket extends ReadStream<DatagramPacket>, Measured {

    /**
     * Write the given {@link Buffer} to the {@link SocketAddress}.
     * The {@link Handler} will be notified once the write completes.
     *
     * @param packet  the {@link Buffer} to write
     * @param port    the host port of the remote peer
     * @param host    the host address of the remote peer
     * @param handler the {@link Handler} to notify once the write completes.
     * @return a reference to this, so the API can be used fluently
     */
    DatagramSocket send(Buffer packet, int port, String host, Handler<AsyncResult<DatagramSocket>> handler);

    /**
     * Returns a {@link PacketWritestream} able to send {@link Buffer} to the
     * {@link SocketAddress}.
     *
     * @param port the port of the remote peer
     * @param host the host address of the remote peer
     * @return the write stream for sending packets
     */
    PacketWritestream sender(int port, String host);

    /**
     * Write the given {@link String} to the {@link SocketAddress} using UTF8 encoding.
     * The {@link Handler} will be notified once the write completes.
     *
     * @param str     the {@link String} to write
     * @param port    the host port of the remote peer
     * @param host    the host address of the remote peer
     * @param handler the {@link Handler} to notify once the write completes.
     * @return a reference to this, so the API can be used fluently
     */
    DatagramSocket send(String str, int port, String host, Handler<AsyncResult<DatagramSocket>> handler);

    /**
     * Write the given {@link String} to the {@link SocketAddress} using the given encoding.
     * The {@link Handler} will be notified once the write completes.
     *
     * @param str     the {@link String} to write
     * @param enc     the charset used for encoding
     * @param port    the host port of the remote peer
     * @param host    the host address of the remote peer
     * @param handler the {@link Handler} to notify once the write completes.
     * @return a reference to this, so the API can be used fluently
     */
    DatagramSocket send(String str, String enc, int port, String host, Handler<AsyncResult<DatagramSocket>> handler);

    /**
     * Closes the {@link DatagramSocket} implementation asynchronous
     * and notifies the handler once done.
     *
     * @param handler the handler to notify once complete
     */
    void close(Handler<AsyncResult<Void>> handler);

    /**
     * Closes the {@link DatagramSocket}. The close itself is asynchronous.
     */
    void close();

    /**
     * Return the {@link SocketAddress} to which
     * this {@link DatagramSocket} is bound.
     *
     * @return the socket address
     */
    SocketAddress localAddress();

    /**
     * Joins a multicast group and listens for packets send to it.
     * The {@link Handler} is notified once the operation completes.
     *
     * @param multicastAddress the address of the multicast group to join
     * @param handler          then handler to notify once the operation completes
     * @return a reference to this, so the API can be used fluently
     */
    DatagramSocket listenMulticastGroup(String multicastAddress, Handler<AsyncResult<DatagramSocket>> handler);

    /**
     * Joins a multicast group and listens for packets send to it on the given network interface.
     * The {@link Handler} is notified once the operation completes.
     *
     * @param multicastAddress the address of the multicast group to join
     * @param networkInterface the network interface on which to listen for packets.
     * @param source           the address of the source for which we will listen for multicast packets
     * @param handler          then handler to notify once the operation completes
     * @return a reference to this, so the API can be used fluently
     */
    DatagramSocket listenMulticastGroup(String multicastAddress, String networkInterface, String source,
                                        Handler<AsyncResult<DatagramSocket>> handler);

    /**
     * Leaves a multicast group and stops listening for packets send to it.
     * The {@link Handler} is notified once the operation completes.
     *
     * @param multicastAddress the address of the multicast group to leave
     * @param handler          then handler to notify once the operation completes
     * @return a reference to this, so the API can be used fluently
     */
    DatagramSocket unlistenMulticastGroup(String multicastAddress, Handler<AsyncResult<DatagramSocket>> handler);

    /**
     * Leaves a multicast group and stops listening for packets send to it on the given network interface.
     * The {@link Handler} is notified once the operation completes.
     *
     * @param multicastAddress the address of the multicast group to join
     * @param networkInterface the network interface on which to listen for packets.
     * @param source           the address of the source for which we will listen for multicast packets
     * @param handler          the handler to notify once the operation completes
     * @return a reference to this, so the API can be used fluently
     */
    DatagramSocket unlistenMulticastGroup(String multicastAddress, String networkInterface, String source,
                                          Handler<AsyncResult<DatagramSocket>> handler);

    /**
     * Block the given address for the given multicast address and notifies the {@link Handler} once
     * the operation completes.
     *
     * @param multicastAddress the address for which you want to block the source address
     * @param sourceToBlock    the source address which should be blocked. You will not receive an multicast packets
     *                         for it anymore.
     * @param handler          the handler to notify once the operation completes
     * @return a reference to this, so the API can be used fluently
     */
    DatagramSocket blockMulticastGroup(String multicastAddress, String sourceToBlock,
                                       Handler<AsyncResult<DatagramSocket>> handler);

    /**
     * Block the given address for the given multicast address on the given network interface and notifies
     * the {@link Handler} once the operation completes.
     *
     * @param multicastAddress the address for which you want to block the source address
     * @param networkInterface the network interface on which the blocking should occur.
     * @param sourceToBlock    the source address which should be blocked. You will not receive an multicast packets
     *                         for it anymore.
     * @param handler          the handler to notify once the operation completes
     * @return a reference to this, so the API can be used fluently
     */
    DatagramSocket blockMulticastGroup(String multicastAddress, String networkInterface, String sourceToBlock,
                                       Handler<AsyncResult<DatagramSocket>> handler);

    /**
     * Start listening on the given port and host. The handler will be called when the socket is listening.
     *
     * @param port    the port to listen on
     * @param host    the host to listen on
     * @param handler the handler will be called when listening
     * @return a reference to this, so the API can be used fluently
     */
    DatagramSocket listen(int port, String host, Handler<AsyncResult<DatagramSocket>> handler);

    @Override
    DatagramSocket pause();

    @Override
    DatagramSocket resume();

    @Override
    DatagramSocket endHandler(Handler<Void> endHandler);

    @Override
    DatagramSocket handler(Handler<DatagramPacket> handler);

    @Override
    DatagramSocket exceptionHandler(Handler<Throwable> handler);

}
