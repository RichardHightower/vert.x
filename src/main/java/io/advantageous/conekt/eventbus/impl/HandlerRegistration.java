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

package io.advantageous.conekt.eventbus.impl;

import io.advantageous.conekt.*;
import io.advantageous.conekt.impl.Arguments;
import io.advantageous.conekt.streams.ReadStream;
import io.advantageous.conekt.eventbus.Message;
import io.advantageous.conekt.eventbus.MessageConsumer;
import io.advantageous.conekt.eventbus.ReplyException;
import io.advantageous.conekt.eventbus.ReplyFailure;
import io.advantageous.conekt.spi.metrics.EventBusMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

/*
 * This class is optimised for performance when used on the same event loop it was created on.
 * However it can be used safely from other threads.
 *
 * The internal state is protected using the synchronized keyword. If always used on the same event loop, then
 * we benefit from biased locking which makes the overhead of synchronized near zero.
 */
public class HandlerRegistration<T> implements MessageConsumer<T>, Handler<Message<T>> {

    public static final int DEFAULT_MAX_BUFFERED_MESSAGES = 1000;

    private static final Logger log = LoggerFactory.getLogger(HandlerRegistration.class);
    private final Conekt conekt;
    private final EventBusMetrics metrics;
    private final EventBusImpl eventBus;
    private final String address;
    private final String repliedAddress;
    private final Handler<AsyncResult<Message<T>>> asyncResultHandler;
    private final Queue<Message<T>> pending = new ArrayDeque<>(8);
    private long timeoutID = -1;
    private boolean registered;
    private Handler<Message<T>> handler;
    private AsyncResult<Void> result;
    private Handler<AsyncResult<Void>> completionHandler;
    private Handler<Void> endHandler;
    private Handler<Message<T>> discardHandler;
    private int maxBufferedMessages = DEFAULT_MAX_BUFFERED_MESSAGES;
    private boolean paused;
    private Object metric;

    public HandlerRegistration(Conekt conekt, EventBusMetrics metrics, EventBusImpl eventBus, String address,
                               String repliedAddress, boolean localOnly,
                               Handler<AsyncResult<Message<T>>> asyncResultHandler, long timeout) {
        this.conekt = conekt;
        this.metrics = metrics;
        this.eventBus = eventBus;
        this.address = address;
        this.repliedAddress = repliedAddress;
        this.asyncResultHandler = asyncResultHandler;
        if (timeout != -1) {
            timeoutID = conekt.setTimer(timeout, tid -> {
                metrics.replyFailure(address, ReplyFailure.TIMEOUT);
                sendAsyncResultFailure(ReplyFailure.TIMEOUT, "Timed out waiting for a reply");
            });
        }
    }

    @Override
    public synchronized MessageConsumer<T> setMaxBufferedMessages(int maxBufferedMessages) {
        Arguments.require(maxBufferedMessages >= 0, "Max buffered messages cannot be negative");
        while (pending.size() > maxBufferedMessages) {
            pending.poll();
        }
        this.maxBufferedMessages = maxBufferedMessages;
        return this;
    }

    @Override
    public synchronized int getMaxBufferedMessages() {
        return maxBufferedMessages;
    }

    @Override
    public String address() {
        return address;
    }

    @Override
    public synchronized void completionHandler(Handler<AsyncResult<Void>> completionHandler) {
        Objects.requireNonNull(completionHandler);
        if (result != null) {
            AsyncResult<Void> value = result;
            conekt.runOnContext(v -> completionHandler.handle(value));
        } else {
            this.completionHandler = completionHandler;
        }
    }

    @Override
    public synchronized void unregister() {
        unregister(false);
    }

    @Override
    public synchronized void unregister(Handler<AsyncResult<Void>> completionHandler) {
        Objects.requireNonNull(completionHandler);
        doUnregister(completionHandler, false);
    }

    public void unregister(boolean callEndHandler) {
        doUnregister(null, callEndHandler);
    }

    public void sendAsyncResultFailure(ReplyFailure failure, String msg) {
        unregister();
        asyncResultHandler.handle(Future.failedFuture(new ReplyException(failure, msg)));
    }

    private void doUnregister(Handler<AsyncResult<Void>> completionHandler, boolean callEndHandler) {
        if (timeoutID != -1) {
            conekt.cancelTimer(timeoutID);
        }
        if (endHandler != null && callEndHandler) {
            Handler<Void> theEndHandler = endHandler;
            Handler<AsyncResult<Void>> handler = completionHandler;
            completionHandler = ar -> {
                theEndHandler.handle(null);
                if (handler != null) {
                    handler.handle(ar);
                }
            };
        }
        if (registered) {
            registered = false;
            eventBus.removeRegistration(address, this, completionHandler);
        } else {
            callCompletionHandlerAsync(completionHandler);
        }
        registered = false;
    }

    private void callCompletionHandlerAsync(Handler<AsyncResult<Void>> completionHandler) {
        if (completionHandler != null) {
            conekt.runOnContext(v -> completionHandler.handle(Future.succeededFuture()));
        }
    }

    public synchronized void setResult(AsyncResult<Void> result) {
        this.result = result;
        if (completionHandler != null) {
            if (result.succeeded()) {
                metric = metrics.handlerRegistered(address, repliedAddress);
            }
            Handler<AsyncResult<Void>> callback = completionHandler;
            conekt.runOnContext(v -> callback.handle(result));
        } else if (result.failed()) {
            log.error("Failed to propagate registration for handler " + handler + " and address " + address);
        } else {
            metric = metrics.handlerRegistered(address, repliedAddress);
        }
    }

    @Override
    public void handle(Message<T> message) {
        Handler<Message<T>> theHandler = null;
        synchronized (this) {
            if (paused) {
                if (pending.size() < maxBufferedMessages) {
                    pending.add(message);
                } else {
                    if (discardHandler != null) {
                        discardHandler.handle(message);
                    } else {
                        log.warn("Discarding message as more than " + maxBufferedMessages + " buffered in paused consumer");
                    }
                }
            } else {
                checkNextTick();
                theHandler = handler;
            }
        }
        // Handle the message outside the sync block
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=473714
        if (theHandler != null) {
            String creditsAddress = message.headers().get(MessageProducerImpl.CREDIT_ADDRESS_HEADER_NAME);
            if (creditsAddress != null) {
                eventBus.send(creditsAddress, 1);
            }
            handleMessage(theHandler, message);
        }
    }

    private void handleMessage(Handler<Message<T>> theHandler, Message<T> message) {
        try {
            theHandler.handle(message);
            metrics.endHandleMessage(metric, null);
        } catch (Exception e) {
            log.error("Failed to handleMessage", e);
            metrics.endHandleMessage(metric, e);
            throw e;
        }
    }

    /*
     * Internal API for testing purposes.
     */
    public synchronized void discardHandler(Handler<Message<T>> handler) {
        this.discardHandler = handler;
    }

    @Override
    public synchronized MessageConsumer<T> handler(Handler<Message<T>> handler) {
        this.handler = handler;
        if (this.handler != null && !registered) {
            registered = true;
            eventBus.addRegistration(address, this, repliedAddress != null, false);
        } else if (this.handler == null && registered) {
            // This will set registered to false
            this.unregister();
        }
        return this;
    }

    @Override
    public ReadStream<T> bodyStream() {
        return new BodyReadStream<>(this);
    }

    @Override
    public synchronized boolean isRegistered() {
        return registered;
    }

    @Override
    public synchronized MessageConsumer<T> pause() {
        if (!paused) {
            paused = true;
        }
        return this;
    }

    @Override
    public synchronized MessageConsumer<T> resume() {
        if (paused) {
            paused = false;
            checkNextTick();
        }
        return this;
    }

    @Override
    public synchronized MessageConsumer<T> endHandler(Handler<Void> endHandler) {
        if (endHandler != null) {
            // We should use the HandlerHolder context to properly do this (needs small refactoring)
            Context endCtx = conekt.getOrCreateContext();
            this.endHandler = v1 -> endCtx.runOnContext(v2 -> endHandler.handle(null));
        } else {
            this.endHandler = null;
        }
        return this;
    }

    @Override
    public synchronized MessageConsumer<T> exceptionHandler(Handler<Throwable> handler) {
        return this;
    }

    private void checkNextTick() {
        // Check if there are more pending messages in the queue that can be processed next time around
        if (!pending.isEmpty()) {
            conekt.runOnContext(v -> {
                if (!paused) {
                    Message<T> message = pending.poll();
                    if (message != null) {
                        HandlerRegistration.this.handle(message);
                    }
                }
            });
        }
    }

    public Handler<Message<T>> getHandler() {
        return handler;
    }

    public Object getMetric() {
        return metric;
    }

}
