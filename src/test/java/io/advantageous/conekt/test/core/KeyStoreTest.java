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

import io.advantageous.conekt.buffer.Buffer;
import io.advantageous.conekt.impl.ConektInternal;
import io.advantageous.conekt.net.*;
import io.advantageous.conekt.net.impl.KeyStoreHelper;
import org.junit.Test;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import java.security.KeyStore;
import java.util.Collections;
import java.util.Enumeration;

import static io.advantageous.conekt.test.core.TestUtils.assertIllegalArgumentException;
import static io.advantageous.conekt.test.core.TestUtils.assertNullPointerException;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class KeyStoreTest extends VertxTestBase {

    @Test
    public void testJKSOptions() throws Exception {
        JksOptions options = new JksOptions();

        assertNull(options.getPath());
        String randString = TestUtils.randomAlphaString(100);
        assertEquals(options, options.setPath(randString));
        assertEquals(randString, options.getPath());

        assertNull(options.getPassword());
        randString = TestUtils.randomAlphaString(100);
        assertEquals(options, options.setPassword(randString));
        assertEquals(randString, options.getPassword());
    }


    @Test
    public void testCopyJKSOptions() throws Exception {
        JksOptions options = new JksOptions();
        String password = TestUtils.randomAlphaString(100);
        String path = TestUtils.randomAlphaString(100);
        Buffer value = Buffer.buffer(TestUtils.randomAlphaString(100));
        options.setPassword(password);
        options.setPath(path);
        options.setValue(value);
        options = new JksOptions(options);
        assertEquals(password, options.getPassword());
        assertEquals(path, options.getPath());
        assertEquals(value, options.getValue());
    }

    @Test
    public void testPKCS12Options() throws Exception {
        PfxOptions options = new PfxOptions();

        assertNull(options.getPath());
        String randString = TestUtils.randomAlphaString(100);
        assertEquals(options, options.setPath(randString));
        assertEquals(randString, options.getPath());

        assertNull(options.getPassword());
        randString = TestUtils.randomAlphaString(100);
        assertEquals(options, options.setPassword(randString));
        assertEquals(randString, options.getPassword());
    }


    @Test
    public void testCopyPKCS12Options() throws Exception {
        PfxOptions options = new PfxOptions();
        String password = TestUtils.randomAlphaString(100);
        String path = TestUtils.randomAlphaString(100);
        Buffer value = Buffer.buffer(TestUtils.randomAlphaString(100));
        options.setPassword(password);
        options.setPath(path);
        options.setValue(value);
        options = new PfxOptions(options);
        assertEquals(password, options.getPassword());
        assertEquals(path, options.getPath());
        assertEquals(value, options.getValue());
    }

    @Test
    public void testKeyCertOptions() throws Exception {
        PemKeyCertOptions options = new PemKeyCertOptions();

        assertNull(options.getKeyPath());
        String randString = TestUtils.randomAlphaString(100);
        assertEquals(options, options.setKeyPath(randString));
        assertEquals(randString, options.getKeyPath());

        assertNull(options.getCertPath());
        randString = TestUtils.randomAlphaString(100);
        assertEquals(options, options.setCertPath(randString));
        assertEquals(randString, options.getCertPath());
    }


    @Test
    public void testTrustOptions() throws Exception {
        PemTrustOptions options = new PemTrustOptions();

        assertEquals(Collections.emptyList(), options.getCertPaths());
        assertNullPointerException(() -> options.addCertPath(null));
        assertIllegalArgumentException(() -> options.addCertPath(""));
        String randString = TestUtils.randomAlphaString(100);
        options.addCertPath(randString);
        assertEquals(Collections.singletonList(randString), options.getCertPaths());

        assertEquals(Collections.emptyList(), options.getCertValues());
        assertNullPointerException(() -> options.addCertValue(null));
        randString = TestUtils.randomAlphaString(100);
        options.addCertValue(Buffer.buffer(randString));
        assertEquals(Collections.singletonList(Buffer.buffer(randString)), options.getCertValues());
    }


    @Test
    public void testJKSPath() throws Exception {
        testKeyStore(getServerCertOptions(KeyCert.JKS));
    }

    @Test
    public void testJKSValue() throws Exception {
        JksOptions options = (JksOptions) getServerCertOptions(KeyCert.JKS);
        Buffer store = conekt.fileSystem().readFileBlocking(options.getPath());
        options.setPath(null).setValue(store);
        testKeyStore(options);
    }

    @Test
    public void testPKCS12Path() throws Exception {
        testKeyStore(getServerCertOptions(KeyCert.PKCS12));
    }

    @Test
    public void testPKCS12Value() throws Exception {
        PfxOptions options = (PfxOptions) getServerCertOptions(KeyCert.PKCS12);
        Buffer store = conekt.fileSystem().readFileBlocking(options.getPath());
        options.setPath(null).setValue(store);
        testKeyStore(options);
    }

    @Test
    public void testKeyCertPath() throws Exception {
        testKeyStore(getServerCertOptions(KeyCert.PEM));
    }

    @Test
    public void testKeyCertValue() throws Exception {
        PemKeyCertOptions options = (PemKeyCertOptions) getServerCertOptions(KeyCert.PEM);
        Buffer key = conekt.fileSystem().readFileBlocking(options.getKeyPath());
        options.setKeyValue(null).setKeyValue(key);
        Buffer cert = conekt.fileSystem().readFileBlocking(options.getCertPath());
        options.setCertValue(null).setCertValue(cert);
        testKeyStore(options);
    }

    @Test
    public void testCaPath() throws Exception {
        testTrustStore(getServerTrustOptions(Trust.PEM));
    }

    @Test
    public void testCaPathValue() throws Exception {
        PemTrustOptions options = (PemTrustOptions) getServerTrustOptions(Trust.PEM);
        options.getCertPaths().
                stream().
                map(conekt.fileSystem()::readFileBlocking).
                forEach(options::addCertValue);
        options.getCertPaths().clear();
        testTrustStore(options);
    }

    @Test
    public void testKeyOptionsEquality() {
        JksOptions jksOptions = (JksOptions) getServerCertOptions(KeyCert.JKS);
        JksOptions jksOptionsCopy = new JksOptions(jksOptions);

        PfxOptions pfxOptions = (PfxOptions) getServerCertOptions(KeyCert.PKCS12);
        PfxOptions pfxOptionsCopy = new PfxOptions(pfxOptions);

        PemKeyCertOptions pemKeyCertOptions = (PemKeyCertOptions) getServerCertOptions(KeyCert.PEM);
        PemKeyCertOptions pemKeyCertOptionsCopy = new PemKeyCertOptions(pemKeyCertOptions);

        assertEquals(jksOptions, jksOptionsCopy);
        assertEquals(jksOptions.hashCode(), jksOptionsCopy.hashCode());

        assertEquals(pfxOptions, pfxOptionsCopy);
        assertEquals(pfxOptions.hashCode(), pfxOptionsCopy.hashCode());

        assertEquals(pemKeyCertOptions, pemKeyCertOptionsCopy);
        assertEquals(pemKeyCertOptions.hashCode(), pemKeyCertOptionsCopy.hashCode());
    }

    private void testKeyStore(KeyCertOptions options) throws Exception {
        KeyStoreHelper helper = KeyStoreHelper.create((ConektInternal) conekt, options);
        KeyStore keyStore = helper.loadStore((ConektInternal) conekt);
        Enumeration<String> aliases = keyStore.aliases();
        assertTrue(aliases.hasMoreElements());
        KeyManager[] keyManagers = helper.getKeyMgrs((ConektInternal) conekt);
        assertTrue(keyManagers.length > 0);
    }

    private void testTrustStore(TrustOptions options) throws Exception {
        KeyStoreHelper helper = KeyStoreHelper.create((ConektInternal) conekt, options);
        TrustManager[] keyManagers = helper.getTrustMgrs((ConektInternal) conekt);
        assertTrue(keyManagers.length > 0);
    }
}
