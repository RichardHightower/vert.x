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

import io.advantageous.conekt.*;
import io.advantageous.conekt.net.*;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class VertxTestBase extends AsyncTestBase {

    protected static final String[] ENABLED_CIPHER_SUITES =
            new String[]{
                    "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
                    "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
                    "TLS_RSA_WITH_AES_128_CBC_SHA256",
                    "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256",
                    "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256",
                    "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
                    "TLS_DHE_DSS_WITH_AES_128_CBC_SHA256",
                    "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
                    "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
                    "TLS_RSA_WITH_AES_128_CBC_SHA",
                    "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA",
                    "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA",
                    "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
                    "TLS_DHE_DSS_WITH_AES_128_CBC_SHA",
                    "TLS_ECDHE_ECDSA_WITH_RC4_128_SHA",
                    "TLS_ECDHE_RSA_WITH_RC4_128_SHA",
                    "SSL_RSA_WITH_RC4_128_SHA",
                    "TLS_ECDH_ECDSA_WITH_RC4_128_SHA",
                    "TLS_ECDH_RSA_WITH_RC4_128_SHA",
                    "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
                    "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                    "TLS_RSA_WITH_AES_128_GCM_SHA256",
                    "TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256",
                    "TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256",
                    "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
                    "TLS_DHE_DSS_WITH_AES_128_GCM_SHA256",
                    "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
                    "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
                    "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
                    "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA",
                    "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA",
                    "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
                    "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA",
                    "SSL_RSA_WITH_RC4_128_MD5",
                    "TLS_EMPTY_RENEGOTIATION_INFO_SCSV",
                    "TLS_DH_anon_WITH_AES_128_GCM_SHA256",
                    "TLS_DH_anon_WITH_AES_128_CBC_SHA256",
                    "TLS_ECDH_anon_WITH_AES_128_CBC_SHA",
                    "TLS_DH_anon_WITH_AES_128_CBC_SHA",
                    "TLS_ECDH_anon_WITH_RC4_128_SHA",
                    "SSL_DH_anon_WITH_RC4_128_MD5",
                    "TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA",
                    "SSL_DH_anon_WITH_3DES_EDE_CBC_SHA",
                    "TLS_RSA_WITH_NULL_SHA256",
                    "TLS_ECDHE_ECDSA_WITH_NULL_SHA",
                    "TLS_ECDHE_RSA_WITH_NULL_SHA",
                    "SSL_RSA_WITH_NULL_SHA",
                    "TLS_ECDH_ECDSA_WITH_NULL_SHA",
                    "TLS_ECDH_RSA_WITH_NULL_SHA",
                    "TLS_ECDH_anon_WITH_NULL_SHA",
                    "SSL_RSA_WITH_NULL_MD5",
                    "SSL_RSA_WITH_DES_CBC_SHA",
                    "SSL_DHE_RSA_WITH_DES_CBC_SHA",
                    "SSL_DHE_DSS_WITH_DES_CBC_SHA",
                    "SSL_DH_anon_WITH_DES_CBC_SHA",
                    "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
                    "SSL_DH_anon_EXPORT_WITH_RC4_40_MD5",
                    "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
                    "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
                    "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA",
                    "SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA",
                    "TLS_KRB5_WITH_RC4_128_SHA",
                    "TLS_KRB5_WITH_RC4_128_MD5",
                    "TLS_KRB5_WITH_3DES_EDE_CBC_SHA",
                    "TLS_KRB5_WITH_3DES_EDE_CBC_MD5",
                    "TLS_KRB5_WITH_DES_CBC_SHA",
                    "TLS_KRB5_WITH_DES_CBC_MD5",
                    "TLS_KRB5_EXPORT_WITH_RC4_40_SHA",
                    "TLS_KRB5_EXPORT_WITH_RC4_40_MD5",
                    "TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA",
                    "TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5"
            };
    private static final Logger log = LoggerFactory.getLogger(VertxTestBase.class);
    @Rule
    public RepeatRule repeatRule = new RepeatRule();
    protected Conekt conekt;
    protected Conekt[] vertices;

    protected void vinit() {
        conekt = null;
        vertices = null;
    }

    public void setUp() throws Exception {
        super.setUp();
        vinit();
        conekt = Conekt.vertx(getOptions());
    }

    protected ConektOptions getOptions() {
        return new ConektOptions();
    }

    protected void tearDown() throws Exception {
        if (conekt != null) {
            CountDownLatch latch = new CountDownLatch(1);
            conekt.close(ar -> {
                latch.countDown();
            });
            awaitLatch(latch);
        }
        if (vertices != null) {
            int numVertices = 0;
            for (int i = 0; i < vertices.length; i++) {
                if (vertices[i] != null) {
                    numVertices++;
                }
            }
            CountDownLatch latch = new CountDownLatch(numVertices);
            for (Conekt conekt : vertices) {
                if (conekt != null) {
                    conekt.close(ar -> {
                        if (ar.failed()) {
                            log.error("Failed to shutdown vert.x", ar.cause());
                        }
                        latch.countDown();
                    });
                }
            }
            assertTrue(latch.await(180, TimeUnit.SECONDS));
        }
        super.tearDown();
    }


    protected void startNodes(int numNodes) {
        startNodes(numNodes, getOptions());
    }

    protected void startNodes(int numNodes, ConektOptions options) {
        CountDownLatch latch = new CountDownLatch(numNodes);
        vertices = new Conekt[numNodes];
        for (int i = 0; i < numNodes; i++) {
            int index = i;
            Conekt.clusteredVertx(options.setClusterHost("localhost").setClusterPort(0).setClustered(false)
                    , ar -> {
                        if (ar.failed()) {
                            ar.cause().printStackTrace();
                        }
                        assertTrue("Failed to start node", ar.succeeded());
                        vertices[index] = ar.result();
                        latch.countDown();
                    });
        }
        try {
            assertTrue(latch.await(2, TimeUnit.MINUTES));
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    protected String findFileOnClasspath(String fileName) {
        URL url = getClass().getClassLoader().getResource(fileName);
        if (url == null) {
            throw new IllegalArgumentException("Cannot find file " + fileName + " on classpath");
        }
        try {
            File file = new File(url.toURI());
            return file.getAbsolutePath();
        } catch (URISyntaxException e) {
            throw new ConektException(e);
        }
    }

    protected void setOptions(TCPSSLOptions sslOptions, KeyCertOptions options) {
        if (options instanceof JksOptions) {
            sslOptions.setKeyStoreOptions((JksOptions) options);
        } else if (options instanceof PfxOptions) {
            sslOptions.setPfxKeyCertOptions((PfxOptions) options);
        } else {
            sslOptions.setPemKeyCertOptions((PemKeyCertOptions) options);
        }
    }

    protected void setOptions(TCPSSLOptions sslOptions, TrustOptions options) {
        if (options instanceof JksOptions) {
            sslOptions.setTrustStoreOptions((JksOptions) options);
        } else if (options instanceof PfxOptions) {
            sslOptions.setPfxTrustOptions((PfxOptions) options);
        } else {
            sslOptions.setPemTrustOptions((PemTrustOptions) options);
        }
    }

    protected TrustOptions getClientTrustOptions(Trust trust) {
        switch (trust) {
            case JKS:
                return new JksOptions().setPath(findFileOnClasspath("tls/client-truststore.jks")).setPassword("wibble");
            case JKS_CA:
                return new JksOptions().setPath(findFileOnClasspath("tls/client-truststore-ca.jks")).setPassword("wibble");
            case PKCS12:
                return new PfxOptions().setPath(findFileOnClasspath("tls/client-truststore.p12")).setPassword("wibble");
            case PKCS12_CA:
                return new PfxOptions().setPath(findFileOnClasspath("tls/client-truststore-ca.p12")).setPassword("wibble");
            case PEM:
                return new PemTrustOptions().addCertPath(findFileOnClasspath("tls/server-cert.pem"));
            case PEM_CA:
                return new PemTrustOptions().addCertPath(findFileOnClasspath("tls/ca/ca-cert.pem"));
            default:
                return null;
        }
    }

    protected KeyCertOptions getClientCertOptions(KeyCert cert) {
        switch (cert) {
            case JKS:
                return new JksOptions().setPath(findFileOnClasspath("tls/client-keystore.jks")).setPassword("wibble");
            case JKS_CA:
                throw new UnsupportedOperationException();
            case PKCS12:
                return new PfxOptions().setPath(findFileOnClasspath("tls/client-keystore.p12")).setPassword("wibble");
            case PKCS12_CA:
                throw new UnsupportedOperationException();
            case PEM:
                return new PemKeyCertOptions().setKeyPath(findFileOnClasspath("tls/client-key.pem")).setCertPath(findFileOnClasspath("tls/client-cert.pem"));
            case PEM_CA:
                return new PemKeyCertOptions().setKeyPath(findFileOnClasspath("tls/client-key.pem")).setCertPath(findFileOnClasspath("tls/client-cert-ca.pem"));
            default:
                return null;
        }
    }

    protected TrustOptions getServerTrustOptions(Trust trust) {
        switch (trust) {
            case JKS:
                return new JksOptions().setPath(findFileOnClasspath("tls/server-truststore.jks")).setPassword("wibble");
            case JKS_CA:
                throw new UnsupportedOperationException();
            case PKCS12:
                return new PfxOptions().setPath(findFileOnClasspath("tls/server-truststore.p12")).setPassword("wibble");
            case PKCS12_CA:
                throw new UnsupportedOperationException();
            case PEM:
                return new PemTrustOptions().addCertPath(findFileOnClasspath("tls/client-cert.pem"));
            case PEM_CA:
                return new PemTrustOptions().addCertPath(findFileOnClasspath("tls/ca/ca-cert.pem"));
            default:
                return null;
        }
    }

    protected KeyCertOptions getServerCertOptions(KeyCert cert) {
        switch (cert) {
            case JKS:
                return new JksOptions().setPath(findFileOnClasspath("tls/server-keystore.jks")).setPassword("wibble");
            case JKS_CA:
                return new JksOptions().setPath(findFileOnClasspath("tls/server-keystore-ca.jks")).setPassword("wibble");
            case PKCS12:
                return new PfxOptions().setPath(findFileOnClasspath("tls/server-keystore.p12")).setPassword("wibble");
            case PKCS12_CA:
                return new PfxOptions().setPath(findFileOnClasspath("tls/server-keystore-ca.p12")).setPassword("wibble");
            case PEM:
                return new PemKeyCertOptions().setKeyPath(findFileOnClasspath("tls/server-key.pem")).setCertPath(findFileOnClasspath("tls/server-cert.pem"));
            case PEM_CA:
                return new PemKeyCertOptions().setKeyPath(findFileOnClasspath("tls/server-key.pem")).setCertPath(findFileOnClasspath("tls/server-cert-ca.pem"));
            default:
                return null;
        }
    }

    /**
     * Create a worker ioActor for the current Vert.x and return its context.
     *
     * @return the context
     * @throws Exception anything preventing the creation of the worker
     */
    protected Context createWorker() throws Exception {
        CompletableFuture<Context> fut = new CompletableFuture<>();
        conekt.deployVerticle(new AbstractIoActor() {
            @Override
            public void start() throws Exception {
                fut.complete(context);
            }
        }, new DeploymentOptions().setWorker(true), ar -> {
            if (ar.failed()) {
                fut.completeExceptionally(ar.cause());
            }
        });
        return fut.get();
    }

    /**
     * Create worker verticles for the current Vert.x and returns the list of their contexts.
     *
     * @param num the number of verticles to create
     * @return the contexts
     * @throws Exception anything preventing the creation of the workers
     */
    protected List<Context> createWorkers(int num) throws Exception {
        List<Context> contexts = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            contexts.add(createWorker());
        }
        return contexts;
    }
}
