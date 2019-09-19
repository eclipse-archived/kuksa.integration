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

import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.Encoder;
import org.eclipse.kuksa.testing.FeignMultipartEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class HawkBitConfiguration {
	@Value("${hawkbit.tenant}")
	private String tenant;
	@Value("${hawkbit.address}")
	private String address;
	@Value("${hawkbit.username}")
	private String username;
	@Value("${hawkbit.password}")
	private String password;

	/**
	 * @return the tenant
	 */
	public String getTenant() {
		return tenant;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return System.getProperty("hawkbit_address") == null ? address : System.getProperty("hawkbit_address");
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		System.setProperty("hawkbit_address", address);
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return System.getProperty("hawkbit_username") == null ? username : System.getProperty("hawkbit_username");

	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		System.setProperty("hawkbit_username", username);
	}

	/**
	 * @return the password
	 */
	public String getPassword()	{
		return System.getProperty("hawkbit_password") == null ? password : System.getProperty("hawkbit_password");
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		System.setProperty("hawkbit_password", password);

	}

	@Bean
	feign.Logger.Level feignLoggerLevel() {
		return feign.Logger.Level.NONE;
	}

	@Bean
	public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
		return new BasicAuthRequestInterceptor(username, password);
	}

	@Bean
	public Encoder feignFormEncoder() {
		return new FeignMultipartEncoder();
	}
}
