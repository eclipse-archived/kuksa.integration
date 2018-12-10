/*
 * ******************************************************************************
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 *  Contributors:
 *      Johannes Kristan (Bosch Software Innovations GmbH) - initial API and functionality
 *      Leon Graser (Bosch Software Innovations GmbH)
 * *****************************************************************************
 */

package org.eclipse.kuksa.testing.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.hono.client.HonoClient;
import org.eclipse.hono.client.MessageConsumer;
import org.eclipse.hono.client.impl.HonoClientImpl;
import org.eclipse.hono.config.ClientConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.proton.ProtonClientOptions;

@Component
public class HonoConnector {

	/* standard logger for information and error output */
	private static final Logger LOGGER = LogManager.getLogger(); // LoggerFactory.getLogger(HonoConnector.class);

	/* connection options for the connection to the Hono Messaging Service */
	private final ProtonClientOptions options;

	/* vertx instance opened to connect to Hono Messaging, needs to be closed */
	private final Vertx vertx = Vertx.vertx();

	/*
	 * client used to connect to the Hono Messaging Service to receive new messages
	 */
	private final HonoClient honoClient;

	/* current number of reconnects so far */
	private int reconnectCount;

	@Autowired
	private ApplicationContext appContext;

	final Future<MessageConsumer> consumerFuture = Future.future();

	/**
	 * Creates a new client to connect to Hono Messaging and forward the received
	 * messages to a message handler of choice.
	 *
	 * @param qpidRouterHost       url of the dispatch router to connect to
	 * @param qpidRouterPort       port of the dispatch router to use
	 * @param honoUser             user to authorize with Hono Messaging
	 * @param honoPassword         password to authorize with Hono Messaging
	 * @param honoTrustedStorePath path to the certificate file used to connect to
	 *                             Hono Messaging
	 * @param reconnectAttempts    maximum number of reconnects
	 * @param honoTenantId         tenant id
	 */

	/*
		public HonoConnector(@Value("${qpid.router.host}") final String qpidRouterHost,
			@Value("${qpid.router.port}") final int qpidRouterPort, @Value("${hono.user}") final String honoUser,
			@Value("${hono.password}") final String honoPassword,
			@Value("${hono.trustedStorePath}") final String honoTrustedStorePath,
			@Value("${hono.reconnectAttempts}") final int reconnectAttempts,
			@Value("${hono.tenant.id}") final String honoTenantId,
			@Value("${hono.device.id}") final String honoDeviceId)
	 */

	public HonoConnector(String qpidRouterHost,
			int qpidRouterPort, String honoUser,
			String honoPassword,
			String honoTrustedStorePath,
			int reconnectAttempts,
			String honoTenantId,
			String honoDeviceId) {

		final ClientConfigProperties messagingProps = new ClientConfigProperties();
		messagingProps.setHost(qpidRouterHost);
		messagingProps.setPort(qpidRouterPort);
		messagingProps.setUsername(honoUser);
		messagingProps.setPassword(honoPassword);
		messagingProps.setTrustStorePath(honoTrustedStorePath);
		messagingProps.setHostnameVerificationRequired(false);

		honoClient = new HonoClientImpl(vertx, messagingProps);
		options = new ProtonClientOptions();
		options.setReconnectAttempts(reconnectAttempts);
		options.setConnectTimeout(10000);
		reconnectCount = 0;
	}

	/**
	 * Reconnect to the hono messaging service. If the number of reconnects exceeds
	 * the number of reconnects defined in the application.properties by the
	 * parameter 'hono.reconnectAttempts' the connector won't reconnect but shutdown
	 * instead.
	 */
	private void reconnect() {
		reconnectCount++;

		if (reconnectCount <= options.getReconnectAttempts()) {
			LOGGER.info("Reconnecting to the Hono Messaging Service...");
			connectToHonoMessaging();
		} else {
			LOGGER.info("Number of reconnects exceeds the user defined threshold of {} reconnects.",
					options.getReconnectAttempts());
			shutdown();
		}
	}

	/**
	 * Shuts down the connector and calls Spring Boot to terminate.
	 */
	private void shutdown() {
		vertx.close();
		LOGGER.info("Shutting connector down...");

		SpringApplication.exit(appContext, () -> 0);
	}

	/*
	 * @Override public void run(final ApplicationArguments applicationArguments) {
	 * // start with the initial connect to Hono connectToHonoMessaging(); }
	 */

	/**
	 * Connects to Hono Messaging using the options and event handler initialized in
	 * the constructor. In case of a exception the client tries the next reconnect.
	 * 
	 * @return
	 */
	public Future<HonoClient> connectToHonoMessaging() {
		try {

			return honoClient.connect(options);
		} catch (Exception e) {
			LOGGER.error("Exception caught during connection attempt to Hono. Reason: {}", e.getMessage());
			e.printStackTrace();
			reconnect();
		}
		return null;
	}
}
