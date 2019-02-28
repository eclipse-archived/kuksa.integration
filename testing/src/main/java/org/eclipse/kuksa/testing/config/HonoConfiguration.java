/*********************************************************************
 * Copyright (c)  2019 Assystem GmbH [and others].
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Assystem GmbH
 **********************************************************************/

package org.eclipse.kuksa.testing.config;

import org.springframework.beans.factory.annotation.Value;
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

}
