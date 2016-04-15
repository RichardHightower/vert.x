/*
 * Copyright (c) 2011-2013 The original author or authors
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

package io.smallvertx.core.impl;

import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.concurrent.GenericFutureListener;
import io.smallvertx.core.*;
import io.smallvertx.core.datagram.DatagramSocket;
import io.smallvertx.core.datagram.DatagramSocketOptions;
import io.smallvertx.core.datagram.impl.DatagramSocketImpl;
import io.smallvertx.core.dns.DnsClient;
import io.smallvertx.core.dns.impl.DnsClientImpl;
import io.smallvertx.core.eventbus.EventBus;
import io.smallvertx.core.eventbus.impl.EventBusImpl;
import io.smallvertx.core.file.FileSystem;
import io.smallvertx.core.file.impl.FileSystemImpl;
import io.smallvertx.core.file.impl.WindowsFileSystem;
import io.smallvertx.core.http.HttpClient;
import io.smallvertx.core.http.HttpClientOptions;
import io.smallvertx.core.http.HttpServer;
import io.smallvertx.core.http.HttpServerOptions;
import io.smallvertx.core.http.impl.HttpClientImpl;
import io.smallvertx.core.http.impl.HttpServerImpl;
import io.smallvertx.core.metrics.impl.DummyVertxMetrics;
import io.smallvertx.core.net.NetClient;
import io.smallvertx.core.net.NetClientOptions;
import io.smallvertx.core.net.NetServer;
import io.smallvertx.core.net.NetServerOptions;
import io.smallvertx.core.net.impl.NetClientImpl;
import io.smallvertx.core.net.impl.NetServerImpl;
import io.smallvertx.core.net.impl.ServerID;
import io.smallvertx.core.spi.VerticleFactory;
import io.smallvertx.core.spi.VertxMetricsFactory;
import io.smallvertx.core.spi.metrics.Metrics;
import io.smallvertx.core.spi.metrics.MetricsProvider;
import io.smallvertx.core.spi.metrics.VertxMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class VertxImpl implements VertxInternal, MetricsProvider {


    private static final Logger log = LoggerFactory.getLogger(VertxImpl.class);

    private static final String NETTY_IO_RATIO_PROPERTY_NAME = "vertx.nettyIORatio";
    private static final int NETTY_IO_RATIO = Integer.getInteger(NETTY_IO_RATIO_PROPERTY_NAME, 50);

    static {
        // Netty resource leak detection has a performance overhead and we do not need it in Vert.x
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
        // Use the JDK deflater/inflater by default
        System.setProperty("io.netty.noJdkZlibDecoder", "false");
    }

    private final FileSystem fileSystem = getFileSystem();
    private final VertxMetrics metrics;
    private final ConcurrentMap<Long, InternalTimerHandler> timeouts = new ConcurrentHashMap<>();
    private final AtomicLong timeoutCounter = new AtomicLong(0);
    private final DeploymentManager deploymentManager;
    private final FileResolver fileResolver;
    private final Map<ServerID, HttpServerImpl> sharedHttpServers = new HashMap<>();
    private final Map<ServerID, NetServerImpl> sharedNetServers = new HashMap<>();
    private final ExecutorService workerPool;
    private final ExecutorService internalBlockingPool;
    private final OrderedExecutorFactory workerOrderedFact;
    private final OrderedExecutorFactory internalOrderedFact;
    private final ThreadFactory eventLoopThreadFactory;
    private final NioEventLoopGroup eventLoopGroup;
    private final NioEventLoopGroup acceptorEventLoopGroup;
    private final BlockedThreadChecker checker;
    private EventBus eventBus;
    private boolean closed;

    VertxImpl() {
        this(new VertxOptions());
    }

    VertxImpl(VertxOptions options) {
        this(options, null);
    }

    VertxImpl(VertxOptions options, Handler<AsyncResult<Vertx>> resultHandler) {
        // Sanity check
        if (Vertx.currentContext() != null) {
            log.warn("You're already on a Vert.x context, are you sure you want to create a new Vertx instance?");
        }
        checker = new BlockedThreadChecker(options.getBlockedThreadCheckInterval(), options.getMaxEventLoopExecuteTime(),
                options.getMaxWorkerExecuteTime(), options.getWarningExceptionTime());
        eventLoopThreadFactory = new VertxThreadFactory("vert.x-eventloop-thread-", checker, false);
        eventLoopGroup = new NioEventLoopGroup(options.getEventLoopPoolSize(), eventLoopThreadFactory);
        eventLoopGroup.setIoRatio(NETTY_IO_RATIO);
        ThreadFactory acceptorEventLoopThreadFactory = new VertxThreadFactory("vert.x-acceptor-thread-", checker, false);
        // The acceptor event loop thread needs to be from a different pool otherwise can get lags in accepted connections
        // under a lot of load
        acceptorEventLoopGroup = new NioEventLoopGroup(1, acceptorEventLoopThreadFactory);
        acceptorEventLoopGroup.setIoRatio(100);
        workerPool = Executors.newFixedThreadPool(options.getWorkerPoolSize(),
                new VertxThreadFactory("vert.x-worker-thread-", checker, true));
        internalBlockingPool = Executors.newFixedThreadPool(options.getInternalBlockingPoolSize(),
                new VertxThreadFactory("vert.x-internal-blocking-", checker, true));
        workerOrderedFact = new OrderedExecutorFactory(workerPool);
        internalOrderedFact = new OrderedExecutorFactory(internalBlockingPool);
        this.fileResolver = new FileResolver(this);
        this.deploymentManager = new DeploymentManager(this);
        this.metrics = initialiseMetrics(options);
        createAndStartEventBus(options, resultHandler);
    }

    public static Context context() {
        Thread current = Thread.currentThread();
        if (current instanceof VertxThread) {
            return ((VertxThread) current).getContext();
        }
        return null;
    }

    private void createAndStartEventBus(VertxOptions options, Handler<AsyncResult<Vertx>> resultHandler) {
        eventBus = new EventBusImpl(this);

        eventBus.start(ar2 -> {
            if (ar2.succeeded()) {
                // If the metric provider wants to use the event bus, it cannot use it in its constructor as the event bus
                // may not be initialized yet. We invokes the eventBusInitialized so it can starts using the event bus.
                metrics.eventBusInitialized(eventBus);

                if (resultHandler != null) {
                    resultHandler.handle(io.smallvertx.core.Future.succeededFuture(this));
                }
            } else {
                log.error("Failed to start event bus", ar2.cause());
            }
        });
    }

    /**
     * @return The FileSystem implementation for the OS
     */
    protected FileSystem getFileSystem() {
        return Utils.isWindows() ? new WindowsFileSystem(this) : new FileSystemImpl(this);
    }

    @Override
    public DatagramSocket createDatagramSocket(DatagramSocketOptions options) {
        return new DatagramSocketImpl(this, options);
    }

    @Override
    public DatagramSocket createDatagramSocket() {
        return createDatagramSocket(new DatagramSocketOptions());
    }

    public NetServer createNetServer(NetServerOptions options) {
        return new NetServerImpl(this, options);
    }

    @Override
    public NetServer createNetServer() {
        return createNetServer(new NetServerOptions());
    }

    public NetClient createNetClient(NetClientOptions options) {
        return new NetClientImpl(this, options);
    }

    @Override
    public NetClient createNetClient() {
        return createNetClient(new NetClientOptions());
    }

    public FileSystem fileSystem() {
        return fileSystem;
    }


    public HttpServer createHttpServer(HttpServerOptions serverOptions) {
        return new HttpServerImpl(this, serverOptions);
    }

    @Override
    public HttpServer createHttpServer() {
        return createHttpServer(new HttpServerOptions());
    }

    public HttpClient createHttpClient(HttpClientOptions options) {
        return new HttpClientImpl(this, options);
    }

    @Override
    public HttpClient createHttpClient() {
        return createHttpClient(new HttpClientOptions());
    }

    public EventBus eventBus() {
        if (eventBus == null) {
            // If reading from different thread possibility that it's been set but not visible - so provide
            // memory barrier
            synchronized (this) {
                return eventBus;
            }
        }
        return eventBus;
    }

    public long setPeriodic(long delay, Handler<Long> handler) {
        return scheduleTimeout(getOrCreateContext(), handler, delay, true);
    }

    @Override
    public TimeoutStream periodicStream(long delay) {
        return new TimeoutStreamImpl(delay, true);
    }

    public long setTimer(long delay, Handler<Long> handler) {
        return scheduleTimeout(getOrCreateContext(), handler, delay, false);
    }

    @Override
    public TimeoutStream timerStream(long delay) {
        return new TimeoutStreamImpl(delay, false);
    }

    public void runOnContext(Handler<Void> task) {
        ContextImpl context = getOrCreateContext();
        context.runOnContext(task);
    }

    // The background pool is used for making blocking calls to legacy synchronous APIs
    public ExecutorService getWorkerPool() {
        return workerPool;
    }

    public EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    public EventLoopGroup getAcceptorEventLoopGroup() {
        return acceptorEventLoopGroup;
    }

    public ContextImpl getOrCreateContext() {
        ContextImpl ctx = getContext();
        if (ctx == null) {
            // We are running embedded - Create a context
            ctx = createEventLoopContext(null, Thread.currentThread().getContextClassLoader());
        }
        return ctx;
    }

    public Map<ServerID, HttpServerImpl> sharedHttpServers() {
        return sharedHttpServers;
    }

    public Map<ServerID, NetServerImpl> sharedNetServers() {
        return sharedNetServers;
    }

    @Override
    public boolean isMetricsEnabled() {
        return metrics != null && metrics.isEnabled();
    }

    @Override
    public Metrics getMetrics() {
        return metrics;
    }

    public boolean cancelTimer(long id) {
        InternalTimerHandler handler = timeouts.remove(id);
        if (handler != null) {
            handler.context.removeCloseHook(handler);
            return handler.cancel();
        } else {
            return false;
        }
    }

    public EventLoopContext createEventLoopContext(String deploymentID, ClassLoader tccl) {
        return new EventLoopContext(this, internalOrderedFact.getExecutor(), workerOrderedFact.getExecutor(),
                deploymentID, tccl);
    }

    @Override
    public DnsClient createDnsClient(int port, String host) {
        return new DnsClientImpl(this, port, host);
    }

    private VertxMetrics initialiseMetrics(VertxOptions options) {
        if (options.getMetricsOptions() != null && options.getMetricsOptions().isEnabled()) {
            ServiceLoader<VertxMetricsFactory> factories = ServiceLoader.load(VertxMetricsFactory.class);
            if (factories.iterator().hasNext()) {
                VertxMetricsFactory factory = factories.iterator().next();
                VertxMetrics metrics = factory.metrics(this, options);
                Objects.requireNonNull(metrics, "The metric instance created from " + factory + " cannot be null");
                return metrics;
            } else {
                log.warn("Metrics has been set to enabled but no VertxMetricsFactory found on classpath");
            }
        }
        return new DummyVertxMetrics();
    }


    private long scheduleTimeout(ContextImpl context, Handler<Long> handler, long delay, boolean periodic) {
        if (delay < 1) {
            throw new IllegalArgumentException("Cannot schedule a timer with delay < 1 ms");
        }
        long timerId = timeoutCounter.getAndIncrement();
        InternalTimerHandler task = new InternalTimerHandler(timerId, handler, periodic, delay, context);
        timeouts.put(timerId, task);
        context.addCloseHook(task);
        return timerId;
    }

    public ContextImpl createWorkerContext(boolean multiThreaded, String deploymentID,
                                           ClassLoader tccl) {
        if (multiThreaded) {
            return new MultiThreadedWorkerContext(this, internalOrderedFact.getExecutor(), workerPool, deploymentID, tccl);
        } else {
            return new WorkerContext(this, internalOrderedFact.getExecutor(), workerOrderedFact.getExecutor(), deploymentID,
                    tccl);
        }
    }

    public ContextImpl getContext() {
        ContextImpl context = (ContextImpl) context();
        if (context != null && context.owner == this) {
            return context;
        }
        return null;
    }


    @Override
    public void close() {
        close(null);
    }

    private void closeClusterManager(Handler<AsyncResult<Void>> completionHandler) {

        completionHandler.handle(io.smallvertx.core.Future.succeededFuture());

    }

    @Override
    public synchronized void close(Handler<AsyncResult<Void>> completionHandler) {
        if (closed || eventBus == null) {
            // Just call the handler directly since pools shutdown
            if (completionHandler != null) {
                completionHandler.handle(io.smallvertx.core.Future.succeededFuture());
            }
            return;
        }
        closed = true;
        deploymentManager.undeployAll(ar -> {

            eventBus.close(ar2 -> {
                closeClusterManager(ar3 -> {
                    // Copy set to prevent ConcurrentModificationException
                    Set<HttpServer> httpServers = new HashSet<>(sharedHttpServers.values());
                    Set<NetServer> netServers = new HashSet<>(sharedNetServers.values());
                    sharedHttpServers.clear();
                    sharedNetServers.clear();

                    int serverCount = httpServers.size() + netServers.size();

                    AtomicInteger serverCloseCount = new AtomicInteger();

                    Handler<AsyncResult<Void>> serverCloseHandler = res -> {
                        if (res.failed()) {
                            log.error("Failure in shutting down server", res.cause());
                        }
                        if (serverCloseCount.incrementAndGet() == serverCount) {
                            deleteCacheDirAndShutdown(completionHandler);
                        }
                    };

                    for (HttpServer server : httpServers) {
                        server.close(serverCloseHandler);
                    }
                    for (NetServer server : netServers) {
                        server.close(serverCloseHandler);
                    }
                    if (serverCount == 0) {
                        deleteCacheDirAndShutdown(completionHandler);
                    }
                });
            });
        });
    }

    @Override
    public void deployVerticle(Verticle verticle) {
        deployVerticle(verticle, new DeploymentOptions(), null);
    }

    @Override
    public void deployVerticle(Verticle verticle, Handler<AsyncResult<String>> completionHandler) {
        deployVerticle(verticle, new DeploymentOptions(), completionHandler);
    }

    @Override
    public void deployVerticle(String name, Handler<AsyncResult<String>> completionHandler) {
        deployVerticle(name, new DeploymentOptions(), completionHandler);
    }

    @Override
    public void deployVerticle(Verticle verticle, DeploymentOptions options) {
        deployVerticle(verticle, options, null);
    }

    @Override
    public void deployVerticle(Verticle verticle, DeploymentOptions options, Handler<AsyncResult<String>> completionHandler) {
        boolean closed;
        synchronized (this) {
            closed = this.closed;
        }
        if (closed) {
            completionHandler.handle(io.smallvertx.core.Future.failedFuture("Vert.x closed"));
        } else {
            deploymentManager.deployVerticle(verticle, options, completionHandler);
        }
    }

    @Override
    public void deployVerticle(String name) {
        deployVerticle(name, new DeploymentOptions(), null);
    }

    @Override
    public void deployVerticle(String name, DeploymentOptions options) {
        deployVerticle(name, options, null);
    }

    @Override
    public void deployVerticle(String name, DeploymentOptions options, Handler<AsyncResult<String>> completionHandler) {
        deploymentManager.deployVerticle(name, options, completionHandler);
    }


    @Override
    public void undeploy(String deploymentID) {
        undeploy(deploymentID, res -> {
        });
    }

    @Override
    public void undeploy(String deploymentID, Handler<AsyncResult<Void>> completionHandler) {
        deploymentManager.undeployVerticle(deploymentID, completionHandler);
    }

    @Override
    public Set<String> deploymentIDs() {
        return deploymentManager.deployments();
    }

    @Override
    public void registerVerticleFactory(VerticleFactory factory) {
        deploymentManager.registerVerticleFactory(factory);
    }

    @Override
    public void unregisterVerticleFactory(VerticleFactory factory) {
        deploymentManager.unregisterVerticleFactory(factory);
    }

    @Override
    public Set<VerticleFactory> verticleFactories() {
        return deploymentManager.verticleFactories();
    }

    @Override
    public <T> void executeBlockingInternal(Action<T> action, Handler<AsyncResult<T>> resultHandler) {
        ContextImpl context = getOrCreateContext();

        context.executeBlocking(action, resultHandler);
    }

    @Override
    public <T> void executeBlocking(Handler<io.smallvertx.core.Future<T>> blockingCodeHandler, boolean ordered,
                                    Handler<AsyncResult<T>> asyncResultHandler) {
        ContextImpl context = getOrCreateContext();
        context.executeBlocking(blockingCodeHandler, ordered, asyncResultHandler);
    }

    @Override
    public <T> void executeBlocking(Handler<io.smallvertx.core.Future<T>> blockingCodeHandler,
                                    Handler<AsyncResult<T>> asyncResultHandler) {
        executeBlocking(blockingCodeHandler, true, asyncResultHandler);
    }


    @Override
    public EventLoopGroup nettyEventLoopGroup() {
        return eventLoopGroup;
    }


    @Override
    public Deployment getDeployment(String deploymentID) {
        return deploymentManager.getDeployment(deploymentID);
    }


    @Override
    public VertxMetrics metricsSPI() {
        return metrics;
    }

    @Override
    public File resolveFile(String fileName) {
        return fileResolver.resolveFile(fileName);
    }

    @SuppressWarnings("unchecked")
    private void deleteCacheDirAndShutdown(Handler<AsyncResult<Void>> completionHandler) {
        fileResolver.close(res -> {

            workerPool.shutdownNow();
            internalBlockingPool.shutdownNow();

            acceptorEventLoopGroup.shutdownGracefully(0, 10, TimeUnit.SECONDS).addListener(new GenericFutureListener() {
                @Override
                public void operationComplete(io.netty.util.concurrent.Future future) throws Exception {
                    if (!future.isSuccess()) {
                        log.warn("Failure in shutting down acceptor event loop group", future.cause());
                    }
                    eventLoopGroup.shutdownGracefully(0, 10, TimeUnit.SECONDS).addListener(new GenericFutureListener() {
                        @Override
                        public void operationComplete(io.netty.util.concurrent.Future future) throws Exception {
                            if (!future.isSuccess()) {
                                log.warn("Failure in shutting down event loop group", future.cause());
                            }
                            if (metrics != null) {
                                metrics.close();
                            }

                            checker.close();

                            if (completionHandler != null) {
                                eventLoopThreadFactory.newThread(() -> {
                                    completionHandler.handle(io.smallvertx.core.Future.succeededFuture());
                                }).start();
                            }
                        }
                    });
                }
            });
        });
    }


    private class InternalTimerHandler implements Handler<Void>, Closeable {
        final Handler<Long> handler;
        final boolean periodic;
        final long timerID;
        final ContextImpl context;
        final java.util.concurrent.Future<?> future;

        InternalTimerHandler(long timerID, Handler<Long> runnable, boolean periodic, long delay, ContextImpl context) {
            this.context = context;
            this.timerID = timerID;
            this.handler = runnable;
            this.periodic = periodic;
            EventLoop el = context.nettyEventLoop();
            Runnable toRun = () -> context.runOnContext(this);
            if (periodic) {
                future = el.scheduleAtFixedRate(toRun, delay, delay, TimeUnit.MILLISECONDS);
            } else {
                future = el.schedule(toRun, delay, TimeUnit.MILLISECONDS);
            }
            metrics.timerCreated(timerID);
        }

        boolean cancel() {
            metrics.timerEnded(timerID, true);
            return future.cancel(false);
        }

        public void handle(Void v) {
            try {
                handler.handle(timerID);
            } finally {
                if (!periodic) {
                    // Clean up after it's fired
                    cleanupNonPeriodic();
                }
            }
        }

        private void cleanupNonPeriodic() {
            VertxImpl.this.timeouts.remove(timerID);
            metrics.timerEnded(timerID, false);
            ContextImpl context = getContext();
            if (context != null) {
                context.removeCloseHook(this);
            }
        }

        // Called via Context close hook when Verticle is undeployed
        public void close(Handler<AsyncResult<Void>> completionHandler) {
            VertxImpl.this.timeouts.remove(timerID);
            cancel();
            completionHandler.handle(io.smallvertx.core.Future.succeededFuture());
        }

    }

    /*
     *
     * This class is optimised for performance when used on the same event loop that is was passed to the handler with.
     * However it can be used safely from other threads.
     *
     * The internal state is protected using the synchronized keyword. If always used on the same event loop, then
     * we benefit from biased locking which makes the overhead of synchronized near zero.
     *
     */
    private class TimeoutStreamImpl implements TimeoutStream, Handler<Long> {

        private final long delay;
        private final boolean periodic;

        private boolean paused;
        private Long id;
        private Handler<Long> handler;
        private Handler<Void> endHandler;

        public TimeoutStreamImpl(long delay, boolean periodic) {
            this.delay = delay;
            this.periodic = periodic;
        }

        @Override
        public synchronized void handle(Long event) {
            try {
                if (!paused) {
                    handler.handle(event);
                }
            } finally {
                if (!periodic && endHandler != null) {
                    endHandler.handle(null);
                }
            }
        }

        @Override
        public TimeoutStream exceptionHandler(Handler<Throwable> handler) {
            return this;
        }

        @Override
        public void cancel() {
            if (id != null) {
                VertxImpl.this.cancelTimer(id);
            }
        }

        @Override
        public synchronized TimeoutStream handler(Handler<Long> handler) {
            if (handler != null) {
                if (id != null) {
                    throw new IllegalStateException();
                }
                this.handler = handler;
                id = scheduleTimeout(getOrCreateContext(), this, delay, periodic);
            } else {
                cancel();
            }
            return this;
        }

        @Override
        public synchronized TimeoutStream pause() {
            this.paused = true;
            return this;
        }

        @Override
        public synchronized TimeoutStream resume() {
            this.paused = false;
            return this;
        }

        @Override
        public synchronized TimeoutStream endHandler(Handler<Void> endHandler) {
            this.endHandler = endHandler;
            return this;
        }
    }
}
