/*********************************************************************
* Copyright (c)  2019 Assystem GmbH [and others].
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors: Assystem GmbH
**********************************************************************/

package org.eclipse.kuksa.testing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppStoreConfiguration {
    @Value("${appstore.address}")
    String appstore_address;

    @Value("${appstore.username}")
    String appstore_username;

    @Value("${appstore.password}")
    String appstore_password;

    public String getAddress() {
        return System.getProperty("appstore_address") == null ? appstore_address : System.getProperty("appstore_address");
    }

    public String getUsername() {
        return System.getProperty("appstore_username") == null ? appstore_username : System.getProperty("appstore_username");

    }

    public String getPassword() {
        return System.getProperty("appstore_password") == null ? appstore_password : System.getProperty("appstore_password");

    }

}
