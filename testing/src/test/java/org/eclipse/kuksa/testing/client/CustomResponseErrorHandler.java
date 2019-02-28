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
