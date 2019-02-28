package org.eclipse.kuksa.testing.appstore;

import org.eclipse.kuksa.testing.AbstractTestCase;
import org.eclipse.kuksa.testing.client.Request;
import org.eclipse.kuksa.testing.config.AppStoreConfiguration;
import org.eclipse.kuksa.testing.model.Credentials;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;

@ContextConfiguration(classes = {AppStoreConfiguration.class})
public abstract class AppStoreTest extends AbstractTestCase {

    // AppStore: /user
    protected static final String JSON_PROPERTY_USER_USERNAME = "username";
    protected static final String JSON_PROPERTY_USER_USERNAME_VALUE = "test.user.username";

    protected static final String JSON_PROPERTY_USER_PASSWORD = "password";
    protected static final String JSON_PROPERTY_USER_PASSWORD_VALUE = "test.user.password";

    protected static final String JSON_PROPERTY_USER_USERTYPE = "userType";
    protected static final String JSON_PROPERTY_USER_USERTYPE_VALUE = "Normal";

    protected static final String JSON_PROPERTY_USER_ADMINUSER = "adminuser";
    protected static final boolean JSON_PROPERTY_USER_ADMINUSER_VALUE = false;

    // AppStore: /category
    protected static final String JSON_PROPERTY_CATEGORY_NAME = "name";
    protected static final String JSON_PROPERTY_CATEGORY_NAME_VALUE = "test.category.name";

    // AppStore: /app
    protected static final String JSON_PROPERTY_APP_NAME = "name";
    protected static final String JSON_PROPERTY_APP_NAME_VALUE = "test.app.name";

    protected static final String JSON_PROPERTY_APP_VERSION = "version";
    protected static final String JSON_PROPERTY_APP_VERSION_VALUE = "test.app.version";

    protected static final String JSON_PROPERTY_APP_HAWKBIT_NAME = "hawkbitname";
    protected static final String JSON_PROPERTY_APP_HAWKBIT_NAME_VALUE = "test.app.hawkbit.name";

    protected static final String JSON_PROPERTY_APP_DESCRIPTION = "description";
    protected static final String JSON_PROPERTY_APP_DESCRIPTION_VALUE = "test.app.description";

    protected static final String JSON_PROPERTY_APP_OWNER = "owner";
    protected static final String JSON_PROPERTY_APP_OWNER_VALUE = "test.app.owner";

    protected static final String JSON_PROPERTY_APP_PUBLISH_DATE = "publishdate";
    protected static final Long JSON_PROPERTY_APP_PUBLISH_DATE_VALUE = new Date().getTime(); // today

    @Autowired
    private AppStoreConfiguration config;

    protected String address;

    protected Credentials credentials;

    @Override
    protected void testSetup() throws Exception {
        address = config.getAddress();
        credentials = new Credentials(config.getUsername(), config.getPassword());
    }

    protected Request.Builder getBaseRequestBuilder() {
        return new Request.Builder()
                .headers(getBaseRequestHeaders())
                .credentials(credentials);
    }

    protected String createUser() throws JSONException {
        Request request = getBaseRequestBuilder()
                .post()
                .url(buildUrl(address, "/api/1.0/user/"))
                .body(new JSONObject()
                        .put(JSON_PROPERTY_USER_ADMINUSER, JSON_PROPERTY_USER_ADMINUSER_VALUE)
                        .put(JSON_PROPERTY_USER_USERNAME, JSON_PROPERTY_USER_USERNAME_VALUE)
                        .put(JSON_PROPERTY_USER_PASSWORD, JSON_PROPERTY_USER_PASSWORD_VALUE)
                        .put(JSON_PROPERTY_USER_USERTYPE, JSON_PROPERTY_USER_USERTYPE_VALUE)
                )
                .build();

        ResponseEntity<String> response = executeApiCall(request);

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to create app store user."));
        }

        return response.getBody();
    }

    protected void removeUser(Long userId) {
        Request request = getBaseRequestBuilder()
                .delete()
                .url(buildUrl(address, "/api/1.0/user/" + userId))
                .build();

        ResponseEntity<String> response = executeApiCall(request);

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to remove app store user."));
        }
    }

    protected String createCategory() throws JSONException {
        Request request = getBaseRequestBuilder()
                .post()
                .url(buildUrl(address, "/api/1.0/appcategory/"))
                .body(new JSONObject()
                        .put(JSON_PROPERTY_CATEGORY_NAME, JSON_PROPERTY_CATEGORY_NAME_VALUE)
                )
                .build();

        ResponseEntity<String> response = executeApiCall(request);

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to create app store category."));
        }

        return response.getBody();
    }

    protected void removeCategory(Long categoryId) {
        Request request = getBaseRequestBuilder()
                .delete()
                .url(buildUrl(address, "/api/1.0/appcategory/" + categoryId))
                .build();

        ResponseEntity<String> response = executeApiCall(request);

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to remove app store category."));
        }
    }

    protected String createApp() throws JSONException {
        Request request = getBaseRequestBuilder()
                .post()
                .url(buildUrl(address, "/api/1.0/app/"))
                .body(new JSONObject()
                        .put(JSON_PROPERTY_APP_NAME, JSON_PROPERTY_APP_NAME_VALUE)
                        .put(JSON_PROPERTY_APP_VERSION, JSON_PROPERTY_APP_VERSION_VALUE)
                        .put(JSON_PROPERTY_APP_DESCRIPTION, JSON_PROPERTY_APP_DESCRIPTION_VALUE)
                        .put(JSON_PROPERTY_APP_HAWKBIT_NAME, JSON_PROPERTY_APP_HAWKBIT_NAME_VALUE)
                        .put(JSON_PROPERTY_APP_OWNER, JSON_PROPERTY_APP_OWNER_VALUE)
                        .put(JSON_PROPERTY_APP_PUBLISH_DATE, JSON_PROPERTY_APP_PUBLISH_DATE_VALUE)
                )
                .build();

        ResponseEntity<String> response = executeApiCall(request);

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to create app store app."));
        }

        return response.getBody();
    }

    protected String removeApp(Long appId) {
        Request request = getBaseRequestBuilder()
                .delete()
                .url(buildUrl(address, "/api/1.0/app/" + appId))
                .build();

        ResponseEntity<String> response = executeApiCall(request);

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to remove app store app."));
        }

        return response.getBody();
    }

}
