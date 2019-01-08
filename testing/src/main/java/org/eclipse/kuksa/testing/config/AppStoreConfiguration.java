package org.eclipse.kuksa.testing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppStoreConfiguration {

    @Value("${appstore.address}")
    private String address;

    @Value("${appstore.username}")
    private String username;

    @Value("${appstore.password}")
    private String password;

    public String getAddress() {
        return address;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
