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

import io.advantageous.conekt.impl.Arguments;
import io.advantageous.conekt.buffer.Buffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Certificate Authority options configuring certificates based on
 * <i>Privacy-enhanced Electronic Email</i> (PEM) files. The options is configured with a list of
 * validating certificates.
 * <p>
 * Validating certificates must contain X.509 certificates wrapped in a PEM block:<p>
 * <p>
 * <pre>
 * -----BEGIN CERTIFICATE-----
 * MIIDezCCAmOgAwIBAgIEVmLkwTANBgkqhkiG9w0BAQsFADBuMRAwDgYDVQQGEwdV
 * ...
 * z5+DuODBJUQst141Jmgq8bS543IU/5apcKQeGNxEyQ==
 * -----END CERTIFICATE-----
 * </pre>
 * <p>
 * The certificates can either be loaded by Vert.x from the filesystem:
 * <p>
 * <pre>
 * HttpServerOptions options = new HttpServerOptions();
 * options.setPemTrustOptions(new PemTrustOptions().addCertPath("/cert.pem"));
 * </pre>
 * <p>
 * Or directly provided as a buffer:
 * <p>
 * <p>
 * <pre>
 * Buffer cert = conekt.fileSystem().readFileSync("/cert.pem");
 * HttpServerOptions options = new HttpServerOptions();
 * options.setPemTrustOptions(new PemTrustOptions().addCertValue(cert));
 * </pre>
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class PemTrustOptions implements TrustOptions, Cloneable {

    private ArrayList<String> certPaths;
    private ArrayList<Buffer> certValues;

    /**
     * Default constructor
     */
    public PemTrustOptions() {
        super();
        this.certPaths = new ArrayList<>();
        this.certValues = new ArrayList<>();
    }

    /**
     * Copy constructor
     *
     * @param other the options to copy
     */
    public PemTrustOptions(PemTrustOptions other) {
        super();
        this.certPaths = new ArrayList<>(other.getCertPaths());
        this.certValues = new ArrayList<>(other.getCertValues());
    }

    /**
     * @return the certificate paths used to locate certificates
     */
    public List<String> getCertPaths() {
        return certPaths;
    }

    /**
     * Add a certificate path
     *
     * @param certPath the path to add
     * @return a reference to this, so the API can be used fluently
     * @throws NullPointerException
     */
    public PemTrustOptions addCertPath(String certPath) throws NullPointerException {
        Objects.requireNonNull(certPath, "No null certificate accepted");
        Arguments.require(!certPath.isEmpty(), "No empty certificate path accepted");
        certPaths.add(certPath);
        return this;
    }

    /**
     * @return the certificate values
     */
    public List<Buffer> getCertValues() {
        return certValues;
    }

    /**
     * Add a certificate value
     *
     * @param certValue the value to add
     * @return a reference to this, so the API can be used fluently
     * @throws NullPointerException
     */
    public PemTrustOptions addCertValue(Buffer certValue) throws NullPointerException {
        Objects.requireNonNull(certValue, "No null certificate accepted");
        certValues.add(certValue);
        return this;
    }

    @Override
    public PemTrustOptions clone() {
        return new PemTrustOptions(this);
    }

}
