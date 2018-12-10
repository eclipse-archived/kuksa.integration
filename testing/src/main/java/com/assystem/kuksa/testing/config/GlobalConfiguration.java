package com.assystem.kuksa.testing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GlobalConfiguration {

	@Value("${DEVICE_ID}")
	private String deviceId;

	@Value("${SECURITY_TOKEN}")
	private String securityToken;

	/**
	 * @return the deviceId
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * @return the securityToken
	 */
	public String getSecurityToken() {
		return securityToken;
	}

}
