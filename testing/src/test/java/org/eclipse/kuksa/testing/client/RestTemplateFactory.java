/*********************************************************************
 * Copyright (c)  2019 Expleo Germany GmbH [and others].
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Expleo Germany GmbH
 **********************************************************************/

package org.eclipse.kuksa.testing.client;

import java.util.Arrays;

import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;

import org.eclipse.kuksa.testing.model.Credentials;

public final class RestTemplateFactory {

	public static RestTemplate getRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new CustomResponseErrorHandler());
		return restTemplate;
	}

	public static RestTemplate getRestTemplateBasiAuth(Credentials credentials) {
		RestTemplate restTemplate = getRestTemplate();
		restTemplate.setInterceptors(Arrays
				.asList(new BasicAuthenticationInterceptor(credentials.getUsername(), credentials.getPassword())));
		return restTemplate;
	}

}
