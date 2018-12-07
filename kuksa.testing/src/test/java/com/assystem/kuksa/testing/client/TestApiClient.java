package com.assystem.kuksa.testing.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import com.assystem.kuksa.testing.model.Credentials;

public class TestApiClient {

	public static final Logger LOGGER = LogManager.getLogger();

	public ResponseEntity<String> executeApiCall(Request request) {

		String url = request.getUrl();
		HttpMethod httpMethod = request.getHttpMethod();
		HttpEntity<String> httpEntity = request.getHttpEntity();
		Credentials credentials = request.getCredentials();

		RestTemplate restTemplate = credentials == null ? RestTemplateFactory.getRestTemplate()
				: RestTemplateFactory.getRestTemplateBasiAuth(credentials);

		logRequest(url, httpMethod, httpEntity);

		ResponseEntity<String> response = restTemplate.exchange(url, httpMethod, httpEntity, String.class);

		logResponse(response);

		return response;
	}

	// LOGGING
	private void logRequest(String url, HttpMethod httpMethod, @Nullable HttpEntity<String> httpEntity) {
		LOGGER.debug("REQUEST {} {}", httpMethod, url);
		if (httpEntity != null) {
			if (httpEntity.getHeaders() != null) {
				HttpHeaders headers = httpEntity.getHeaders();
				LOGGER.debug("HEADERS {}", headers);
			}
			if (httpEntity.hasBody()) {
				String body = httpEntity.getBody();
				LOGGER.debug("BODY {}", body);
			}
		}
	}

	private void logResponse(ResponseEntity<String> response) {
		LOGGER.debug("RESPONSE {}", response.getStatusCode());
		if (response.hasBody()) {
			String body = response.getBody();
			LOGGER.debug("BODY {}", body);
		}
	}

}
