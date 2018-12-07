package com.assystem.kuksa.testing.client;

import java.io.IOException;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

/**
 * Custom implementation of {@link DefaultResponseErrorHandler}.
 * {@link DefaultResponseErrorHandler} throws an exception when a client error
 * or a server occurs. This mechanic is override in this class to return an
 * appropriate response to the test case.
 * 
 * @author cnguyen
 *
 */
public class CustomResponseErrorHandler extends DefaultResponseErrorHandler {

	@Override
	public boolean hasError(ClientHttpResponse response) throws IOException {
		// return always false -> response will be evaluated in test case
		return false;
	}

}
