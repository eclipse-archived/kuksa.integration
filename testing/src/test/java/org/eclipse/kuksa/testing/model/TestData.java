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

package org.eclipse.kuksa.testing.model;

import java.util.Map;

public class TestData {

    /**
     * HONO
     */
    private String tenantId;

    private String deviceId;

    private String authId;

    private String username;

    private String password;

    private String token;

    private Map<String, String> header;

    private String body;

    /**
     * Appstore
     */

    private String appname;

    /**
     * HawkBit
     */
    private String controllerId;

    private String description;

    private String name;

    private String target;


    public String getTenantId() {
        return tenantId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getAuthId() {
        return authId;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }

    public String getUsername() {
        return username;
    }

    public String getAppname() {
        return appname;
    }

}
