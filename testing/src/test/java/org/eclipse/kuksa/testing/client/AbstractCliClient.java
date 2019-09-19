/*******************************************************************************
 * Copyright (c) 2016, 2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * Additional configuration for Kuksa: Expleo Germany GmbH
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.kuksa.testing.client;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.eclipse.hono.client.ApplicationClientFactory;
import org.eclipse.kuksa.testing.config.HonoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

/**
 * Abstract base class for the Hono CLI module.
 */
public abstract class AbstractCliClient {

    protected ApplicationClientFactory clientFactory = HonoConfiguration.clientFactory() ;

    /**
     * A logger to be shared with subclasses.
     */
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    /**
     * The vert.x instance to run on.
     */
    protected Vertx vertx;
    /**
     * The vert.x context to run on.
     */
    protected Context ctx;
    /**
     * Sets the vert.x instance.
     * 
     * @param vertx The vert.x instance.
     * @throws NullPointerException if vert.x is {@code null}.
     */
    @Autowired
    public final void setVertx(final Vertx vertx) {
        this.vertx = Objects.requireNonNull(vertx);
        this.ctx = vertx.getOrCreateContext();
    }

}
