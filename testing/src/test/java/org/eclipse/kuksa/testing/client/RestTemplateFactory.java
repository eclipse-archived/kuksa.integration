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
