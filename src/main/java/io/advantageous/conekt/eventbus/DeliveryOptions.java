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

import io.advantageous.conekt.impl.Arguments;
import io.advantageous.conekt.MultiMap;
import io.advantageous.conekt.http.CaseInsensitiveHeaders;

import java.util.Objects;

/**
 * Delivery options are used to configure message delivery.
 * <p>
 * Delivery options allow to configure delivery timeout and message codec name, and to provide any headers
 * that you wish to send with the message.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class DeliveryOptions {

    /**
     * The default send timeout.
     */
    public static final long DEFAULT_TIMEOUT = 30 * 1000;

    private long timeout = DEFAULT_TIMEOUT;
    private String codecName;
    private MultiMap headers;

    /**
     * Default constructor
     */
    public DeliveryOptions() {
    }

    /**
     * Copy constructor
     *
     * @param other the options to copy
     */
    public DeliveryOptions(DeliveryOptions other) {
        this.timeout = other.getSendTimeout();
        this.codecName = other.getCodecName();
        this.headers = other.getHeaders();
    }


    /**
     * Get the send timeout.
     * <p>
     * When sending a message with a response handler a send timeout can be provided. If no response is received
     * within the timeout the handler will be called with a failure.
     *
     * @return the value of send timeout
     */
    public long getSendTimeout() {
        return timeout;
    }

    /**
     * Set the send timeout.
     *
     * @param timeout the timeout value, in ms.
     * @return a reference to this, so the API can be used fluently
     */
    public DeliveryOptions setSendTimeout(long timeout) {
        Arguments.require(timeout >= 1, "sendTimeout must be >= 1");
        this.timeout = timeout;
        return this;
    }

    /**
     * Get the codec name.
     * <p>
     * When sending or publishing a message a codec name can be provided. This must correspond with a previously registered
     * message codec. This allows you to send arbitrary objects on the event bus (e.g. POJOs).
     *
     * @return the codec name
     */
    public String getCodecName() {
        return codecName;
    }

    /**
     * Set the codec name.
     *
     * @param codecName the codec name
     * @return a reference to this, so the API can be used fluently
     */
    public DeliveryOptions setCodecName(String codecName) {
        this.codecName = codecName;
        return this;
    }

    /**
     * Add a message header.
     * <p>
     * Message headers can be sent with any message and will be accessible with {@link Message#headers}
     * at the recipient.
     *
     * @param key   the header key
     * @param value the header value
     * @return a reference to this, so the API can be used fluently
     */
    public DeliveryOptions addHeader(String key, String value) {
        checkHeaders();
        Objects.requireNonNull(key, "no null key accepted");
        Objects.requireNonNull(value, "no null value accepted");
        headers.add(key, value);
        return this;
    }

    /**
     * Get the message headers
     *
     * @return the headers
     */
    public MultiMap getHeaders() {
        return headers;
    }

    /**
     * Set message headers from a multi-map.
     *
     * @param headers the headers
     * @return a reference to this, so the API can be used fluently
     */
    public DeliveryOptions setHeaders(MultiMap headers) {
        this.headers = headers;
        return this;
    }

    private void checkHeaders() {
        if (headers == null) {
            headers = new CaseInsensitiveHeaders();
        }
    }
}
