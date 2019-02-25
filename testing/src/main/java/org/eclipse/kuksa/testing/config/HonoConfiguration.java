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

	@Value("${HONO_DEVICE_REGISTRY_STABLE}")
	private String deviceRegistryStable;

	@Value("${HONO_DISPATCH_ROUTER_STABLE}")
	private String dispatchRouterStable;

	@Value("${HONO_ADAPTER_HTTP_VERTX_STABLE}")
	private String adapterHttpVertxStable;

	@Value("${HONO_ADAPTER_MQTT_VERTX_STABLE}")
	private String adapterMqttVertxStable;

	/**
	 * @return the deviceRegistryStable
	 */
	public String getDeviceRegistryStable() { return System.getProperty("hono_device_registry"); }

	/**
	 * @return the dispatchRouterStable
	 */
	public String getDispatchRouterStable() {
		return System.getProperty("hono_dispatch_router");
	}

	/**
	 * @return the adapterHttpVertxStable
	 */
	public String getAdapterHttpVertxStable() {
		return System.getProperty("hono_adapter_http_vertx");
	}

	/**
	 * @return the adapterHttpVertxStable
	 */
	public String getAdapterMqttVertxStable() {
		return System.getProperty("hono_adapter_mqtt_vertx");
	}

}
