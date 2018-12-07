package com.assystem.kuksa.testing.config;

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

	/**
	 * @return the deviceRegistryStable
	 */
	public String getDeviceRegistryStable() {
		return deviceRegistryStable;
	}

	/**
	 * @return the dispatchRouterStable
	 */
	public String getDispatchRouterStable() {
		return dispatchRouterStable;
	}

	/**
	 * @return the adapterHttpVertxStable
	 */
	public String getAdapterHttpVertxStable() {
		return adapterHttpVertxStable;
	}

}
