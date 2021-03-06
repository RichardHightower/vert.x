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

package io.advantageous.conekt.impl;

import io.advantageous.conekt.AsyncResult;
import io.advantageous.conekt.CompositeFuture;
import io.advantageous.conekt.Future;
import io.advantageous.conekt.Handler;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class CompositeFutureImpl implements CompositeFuture, Handler<AsyncResult<CompositeFuture>> {

    private final Future[] results;
    private int flag;
    private boolean completed;
    private Throwable cause;
    private Handler<AsyncResult<CompositeFuture>> handler;

    private CompositeFutureImpl(Future<?>... results) {
        this.results = results;
    }

    public static CompositeFuture all(Future<?>... results) {
        CompositeFutureImpl composite = new CompositeFutureImpl(results);
        for (int i = 0; i < results.length; i++) {
            int index = i;
            results[i].setHandler(ar -> {
                Handler<AsyncResult<CompositeFuture>> handler = null;
                if (ar.succeeded()) {
                    synchronized (composite) {
                        composite.flag |= 1 << index;
                        if (!composite.isComplete() && composite.flag == (1 << results.length) - 1) {
                            handler = composite.setSucceeded();
                        }
                    }
                } else {
                    synchronized (composite) {
                        if (!composite.isComplete()) {
                            handler = composite.setFailed(ar.cause());
                        }
                    }
                }
                if (handler != null) {
                    handler.handle(composite);
                }
            });
        }
        return composite;
    }

    public static CompositeFuture any(Future<?>... results) {
        CompositeFutureImpl composite = new CompositeFutureImpl(results);
        for (int i = 0; i < results.length; i++) {
            int index = i;
            results[i].setHandler(ar -> {
                Handler<AsyncResult<CompositeFuture>> handler = null;
                if (ar.succeeded()) {
                    synchronized (composite) {
                        if (!composite.isComplete()) {
                            composite.setSucceeded();
                        }
                    }
                } else {
                    synchronized (composite) {
                        composite.flag |= 1 << index;
                        if (!composite.isComplete() && composite.flag == (1 << results.length) - 1) {
                            handler = composite.setFailed(ar.cause());
                        }
                    }
                }
                if (handler != null) {
                    handler.handle(composite);
                }
            });
        }
        return composite;
    }

    @Override
    public CompositeFuture setHandler(Handler<AsyncResult<CompositeFuture>> handler) {
        boolean call;
        synchronized (this) {
            this.handler = handler;
            call = completed;
        }
        if (call) {
            handler.handle(this);
        }
        return this;
    }

    @Override
    public Throwable cause(int index) {
        return future(index).cause();
    }

    @Override
    public boolean succeeded(int index) {
        return future(index).succeeded();
    }

    @Override
    public boolean failed(int index) {
        return future(index).failed();
    }

    @Override
    public boolean isComplete(int index) {
        return future(index).isComplete();
    }

    @Override
    public <T> T result(int index) {
        return this.<T>future(index).result();
    }

    private <T> Future<T> future(int index) {
        if (index < 0 || index > results.length) {
            throw new IndexOutOfBoundsException();
        }
        return (Future<T>) results[index];
    }

    @Override
    public int size() {
        return results.length;
    }

    @Override
    public synchronized boolean isComplete() {
        return completed;
    }

    @Override
    public synchronized boolean succeeded() {
        return completed && cause == null;
    }

    @Override
    public synchronized boolean failed() {
        return completed && cause != null;
    }

    @Override
    public synchronized Throwable cause() {
        return completed && cause != null ? cause : null;
    }

    @Override
    public synchronized CompositeFuture result() {
        return completed && cause == null ? this : null;
    }

    @Override
    public void complete(CompositeFuture result) {
        Handler<AsyncResult<CompositeFuture>> handler = setSucceeded();
        if (handler != null) {
            handler.handle(this);
        }
    }

    @Override
    public void complete() {
        complete(null);
    }

    @Override
    public void fail(Throwable throwable) {
        Handler<AsyncResult<CompositeFuture>> handler = setFailed(throwable);
        if (handler != null) {
            handler.handle(this);
        }
    }

    @Override
    public void fail(String failureMessage) {
        fail(new NoStackTraceThrowable(failureMessage));
    }

    private Handler<AsyncResult<CompositeFuture>> setFailed(Throwable cause) {
        synchronized (this) {
            if (completed) {
                throw new IllegalStateException("Result is already complete: " + (this.cause == null ? "succeeded" : "failed"));
            }
            this.completed = true;
            this.cause = cause;
            return handler;
        }
    }

    private Handler<AsyncResult<CompositeFuture>> setSucceeded() {
        synchronized (this) {
            if (completed) {
                throw new IllegalStateException("Result is already complete: " + (this.cause == null ? "succeeded" : "failed"));
            }
            this.completed = true;
            return handler;
        }
    }

    @Override
    public Handler<AsyncResult<CompositeFuture>> completer() {
        return this;
    }

    @Override
    public void handle(AsyncResult<CompositeFuture> ar) {
        if (ar.succeeded()) {
            complete(this);
        } else {
            fail(ar.cause());
        }
    }
}
