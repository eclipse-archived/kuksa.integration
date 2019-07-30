/*********************************************************************
 * Copyright (c)  2019 Expleo GmbH [and others].
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Expleo GmbH
 **********************************************************************/

package org.eclipse.kuksa.testing.config;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import org.eclipse.hono.client.ApplicationClientFactory;
import org.eclipse.hono.client.HonoConnection;
import org.eclipse.hono.config.ClientConfigProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public final class HonoConfiguration {
	@Value("${hono_device_registry}")
	private static String hono_device_registry;

	@Value("${hono_dispatch_router}")
	private static String hono_dispatch_router;

	@Value("${hono_adapter_http_vertx}")
	private static String hono_adapter_http_vertx;

	@Value("${hono_adapter_mqtt_vertx}")
	private static String hono_adapter_mqtt_vertx;

	@Value("${hono_client_username}")
	private static String hono_client_username;

	@Value("${hono_client_password}")
	private static String hono_client_password;

	@Value("${hono_client_host}")
	private static String hono_client_host;

	@Value("${hono_client_port}")
	private static int hono_client_port;

	/**
	 * @return the deviceRegistryStable
	 */
	public static String getDeviceRegistryStable() {
		return System.getProperty("hono_device_registry") == null ? hono_device_registry : System.getProperty("hono_device_registry");
	}

	/**
	 * @return the dispatchRouterStable
	 */
	public static String getDispatchRouterStable() {
		return System.getProperty("hono_dispatch_router") == null ? hono_dispatch_router : System.getProperty("hono_dispatch_router");
	}

	/**
	 * @return the adapterHttpVertxStable
	 */
	public static String getAdapterHttpVertxStable() {
		return System.getProperty("hono_adapter_http_vertx") == null ? hono_adapter_http_vertx : System.getProperty("hono_adapter_http_vertx");
	}

	/**
	 * @return the adapterHttpVertxStable
	 */
	public static String getAdapterMqttVertxStable() {
		return System.getProperty("hono_adapter_mqtt_vertx") == null ? hono_adapter_mqtt_vertx : System.getProperty("hono_adapter_mqtt_vertx");

	}

	public static String getHono_client_username() {
		return System.getProperty("hono_client_username");
	}

	public static String getHono_client_password() {
		return System.getProperty("hono_client_password");
	}

	public static String getHono_client_host() {
		return System.getProperty("hono_client_host");
	}

	public static int getHono_client_port() {
		return Integer.parseInt(System.getProperty("hono_client_port"));
	}

	/**
	 * Exposes a Vert.x instance as a Spring bean.
	 *
	 * @return The Vert.x instance.
	 */
	@Bean
	public static Vertx vertx() {
		final VertxOptions options = new VertxOptions()
				.setWarningExceptionTime(1500000000)
				.setAddressResolverOptions(addressResolverOptions());
		return Vertx.vertx(options);
	}

	/**
	 * Exposes address resolver option properties as a Spring bean.
	 *
	 * @return The properties.
	 */
	@ConfigurationProperties(prefix = "address.resolver")

	public static AddressResolverOptions addressResolverOptions() {
		final AddressResolverOptions addressResolverOptions = new AddressResolverOptions();
		return addressResolverOptions;
	}

	/**
	 * Exposes connection configuration properties as a Spring bean.
	 *
	 * @return The properties.
	 */
//	@ConfigurationProperties(prefix = "hono.client")

	public static ClientConfigProperties honoClientConfig() {
		final ClientConfigProperties config = new ClientConfigProperties();
		config.setHost(getHono_client_host());
		config.setUsername(getHono_client_username());
		config.setPort(getHono_client_port());
		config.setPassword(getHono_client_password());
		config.setTrustStorePath(new HonoConfiguration().getClass().getClassLoader().getResource("DSTRootX3.pem").getFile());
		config.setReconnectAttempts(5);
		config.setTlsEnabled(false);
		config.setHostnameVerificationRequired(false);

		System.out.println("CONFIGURATION " + config.getUsername());

		return config;
	}

	/**
	 * Exposes a factory for creating clients for Hono's northbound APIs as a Spring bean.
	 *
	 * @return The factory.
	 */

	public static ApplicationClientFactory clientFactory() {
		return ApplicationClientFactory.create(HonoConnection.newConnection(vertx(), honoClientConfig()));
	}

}
