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

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import org.eclipse.kuksa.testing.model.Credentials;

public class Request {

	private String url;

	private HttpMethod httpMethod;

	private HttpEntity<String> httpEntity;

	private Credentials credentials;

	public Request(String url, HttpMethod httpMethod, HttpEntity<String> httpEntity, Credentials credentials) {
		super();
		this.url = url;
		this.httpMethod = httpMethod;
		this.httpEntity = httpEntity;
		this.credentials = credentials;
	}

	public String getUrl() {
		return url;
	}

	public HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public HttpEntity<String> getHttpEntity() {
		return httpEntity;
	}

	public Credentials getCredentials() {
		return credentials;
	}

	public static class Builder {

		private String url;

		private HttpMethod method;

		private HttpHeaders headers;

		private String body;

		private Credentials credentials;

		public Builder url(String url) {
			this.url = url;
			return this;
		}

		public Builder httpMethod(HttpMethod method) {
			this.method = method;
			return this;
		}

		public Builder get() {
			return httpMethod(HttpMethod.GET);
		}

		public Builder post() {
			return httpMethod(HttpMethod.POST);
		}

		public Builder delete() {
			return httpMethod(HttpMethod.DELETE);
		}

		public Builder put() {
			return httpMethod(HttpMethod.PUT);
		}

		public Builder headers(HttpHeaders headers) {
			this.headers = headers;
			return this;
		}

		public Builder addHeader(String key, String value) {
			if (headers == null) {
				headers = new HttpHeaders();
			}
			headers.add(key, value);
			return this;
		}

		public Builder body(String body) {
			this.body = body;
			return this;
		}

		public Builder body(Object body) {
			this.body = body.toString();
			return this;
		}

		public Builder credentials(Credentials credentials) {
			this.credentials = credentials;
			return this;
		}

		public Builder credentials(String username, String password) {
			this.credentials = new Credentials(username, password);
			return this;
		}

		public Request build() {
			return new Request(url, method, body != null ? new HttpEntity<>(body, headers) : new HttpEntity<>(headers),
					credentials);
		}

	}

}
