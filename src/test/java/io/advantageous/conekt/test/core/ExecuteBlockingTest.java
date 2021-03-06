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

package io.advantageous.conekt.test.core;

import io.advantageous.conekt.Context;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ExecuteBlockingTest extends VertxTestBase {

    @Test
    public void testExecuteBlockingSuccess() {

        conekt.executeBlocking(future -> {
            try {
                Thread.sleep(1000);
            } catch (Exception ignore) {
            }
            future.complete("done!");
        }, onSuccess(res -> {
            assertEquals("done!", res);
            testComplete();
        }));
        await();
    }

    @Test
    public void testExecuteBlockingFailed() {

        conekt.executeBlocking(future -> {
            try {
                Thread.sleep(1000);
            } catch (Exception ignore) {
            }
            future.fail("failed!");
        }, onFailure(t -> {
            assertEquals("failed!", t.getMessage());
            testComplete();
        }));
        await();
    }

    @Test
    public void testExecuteBlockingThrowsRTE() {

        conekt.executeBlocking(future -> {
            throw new RuntimeException("rte");
        }, onFailure(t -> {
            assertEquals("rte", t.getMessage());
            testComplete();
        }));
        await();
    }

    @Test
    public void testExecuteBlockingContext() {

        conekt.runOnContext(v -> {
            Context ctx = conekt.getOrCreateContext();
            assertTrue(ctx.isEventLoopContext());
            conekt.executeBlocking(future -> {
                assertSame(ctx, conekt.getOrCreateContext());
                assertTrue(Thread.currentThread().getName().startsWith("vert.x-worker-thread"));
                assertTrue(Context.isOnWorkerThread());
                assertFalse(Context.isOnEventLoopThread());
                try {
                    Thread.sleep(1000);
                } catch (Exception ignore) {
                }
                conekt.runOnContext(v2 -> {
                    assertSame(ctx, conekt.getOrCreateContext());
                    assertTrue(Thread.currentThread().getName().startsWith("vert.x-eventloop-thread"));
                    assertFalse(Context.isOnWorkerThread());
                    assertTrue(Context.isOnEventLoopThread());
                    future.complete("done!");
                });
            }, onSuccess(res -> {
                assertSame(ctx, conekt.getOrCreateContext());
                assertTrue(Thread.currentThread().getName().startsWith("vert.x-eventloop-thread"));
                assertFalse(Context.isOnWorkerThread());
                assertTrue(Context.isOnEventLoopThread());
                assertEquals("done!", res);
                testComplete();
            }));
        });

        await();
    }

    @Test
    public void testExecuteBlockingTTCL() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        assertNotNull(cl);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ClassLoader> blockingTCCL = new AtomicReference<>();
        conekt.<String>executeBlocking(future -> {
            future.complete("whatever");
            blockingTCCL.set(Thread.currentThread().getContextClassLoader());
        }, ar -> {
            assertTrue(ar.succeeded());
            assertEquals("whatever", ar.result());
            latch.countDown();
        });
        assertSame(cl, Thread.currentThread().getContextClassLoader());
        awaitLatch(latch);
        assertSame(cl, blockingTCCL.get());
    }

    @Test
    public void testExecuteBlockingParallel() throws Exception {

        long start = System.currentTimeMillis();
        int numExecBlocking = 10;
        long pause = 1000;
        CountDownLatch latch = new CountDownLatch(numExecBlocking);

        conekt.runOnContext(v -> {
            Context ctx = conekt.getOrCreateContext();
            assertTrue(ctx.isEventLoopContext());

            for (int i = 0; i < numExecBlocking; i++) {
                conekt.executeBlocking(future -> {
                    assertSame(ctx, conekt.getOrCreateContext());
                    assertTrue(Thread.currentThread().getName().startsWith("vert.x-worker-thread"));
                    assertTrue(Context.isOnWorkerThread());
                    assertFalse(Context.isOnEventLoopThread());
                    try {
                        Thread.sleep(pause);
                    } catch (Exception ignore) {
                    }
                    future.complete("done!");
                }, false, onSuccess(res -> {
                    assertSame(ctx, conekt.getOrCreateContext());
                    assertTrue(Thread.currentThread().getName().startsWith("vert.x-eventloop-thread"));
                    assertFalse(Context.isOnWorkerThread());
                    assertTrue(Context.isOnEventLoopThread());
                    assertEquals("done!", res);
                    latch.countDown();

                }));
            }
        });

        awaitLatch(latch);

        long now = System.currentTimeMillis();
        long leeway = 1000;
        assertTrue(now - start < pause + leeway);
    }
}
