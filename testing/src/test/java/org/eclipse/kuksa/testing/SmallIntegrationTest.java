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

package org.eclipse.kuksa.testing;

import org.apache.logging.log4j.Level;
import org.eclipse.kuksa.testing.client.Request;
import org.eclipse.kuksa.testing.config.AppStoreConfiguration;
import org.eclipse.kuksa.testing.config.GlobalConfiguration;
import org.eclipse.kuksa.testing.config.HawkBitConfiguration;
import org.eclipse.kuksa.testing.model.Credentials;
import org.eclipse.kuksa.testing.model.ResponseResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.PostConstruct;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@Ignore
@ContextConfiguration(classes = {GlobalConfiguration.class, HawkBitConfiguration.class, AppStoreConfiguration.class})
public class SmallIntegrationTest extends AbstractTestCase {

    private static final String JSON_PROPERTY_USER_USERNAME = "username";
    private static final String JSON_PROPERTY_USER_USERNAME_VALUE = "test.user.username";

    private static final String JSON_PROPERTY_USER_PASSWORD = "password";
    private static final String JSON_PROPERTY_USER_PASSWORD_VALUE = "test.user.password";

    private static final String JSON_PROPERTY_USER_USERTYPE = "userType";
    private static final String JSON_PROPERTY_USER_USERTYPE_VALUE = "Normal";

    private static final String JSON_PROPERTY_USER_ADMINUSER = "adminuser";
    private static final boolean JSON_PROPERTY_USER_ADMINUSER_VALUE = false;

    // AppStore: /category
    private static final String JSON_PROPERTY_CATEGORY_NAME = "name";
    private static final String JSON_PROPERTY_CATEGORY_NAME_VALUE = "test.app.category";

    // AppStore: /app
    private static final String JSON_PROPERTY_APP_NAME = "name";
    private static final String JSON_PROPERTY_APP_NAME_VALUE = "test.app.name";

    private static final String JSON_PROPERTY_APP_VERSION = "version";
    private static final String JSON_PROPERTY_APP_VERSION_VALUE = "1.0";

    private static final String JSON_PROPERTY_APP_HAWKBIT_NAME = "hawkbitname";
    private static final String JSON_PROPERTY_APP_HAWKBIT_NAME_VALUE = "test.app.hawkbit.name";

    private static final String JSON_PROPERTY_APP_DESCRIPTION = "description";
    private static final String JSON_PROPERTY_APP_DESCRIPTION_VALUE = "test.app.description";

    private static final String JSON_PROPERTY_APP_OWNER = "owner";
    private static final String JSON_PROPERTY_APP_OWNER_VALUE = "test.app.owner";

    private static final String JSON_PROPERTY_APP_DOWNLOADCOUNT = "downloadcount";
    private static final int JSON_PROPERTY_DOWNLOADCOUNT_VALUE = 0;

    private static final String JSON_PROPERTY_APP_PUBLISH_DATE = "publishdate";
    private static final Long JSON_PROPERTY_APP_PUBLISH_DATE_VALUE = new Date().getTime(); // today

    private static final String JSON_PROPERTY_APP_CATEGORY_NAME = "appcategory";
    private static JSONObject JSON_PROPERTY_APP_CATEGORY_NAME_VALUE = new JSONObject();

    @Autowired
    private GlobalConfiguration global;

    @Autowired
    private HawkBitConfiguration hawkbit;

    @Autowired
    private AppStoreConfiguration appstore;

    private static GlobalConfiguration globalConfig;

    private static HawkBitConfiguration hawkbitConfig;

    private static AppStoreConfiguration appstoreConfig;

    private static String securityToken;

    private static String deviceId;

    private static Credentials hawkbitCredentials;

    private static String appstoreAddress;

    private static String controllerId;

    private static final String JSON_PROPERTY_ID = "id";

    private static Long userId;

    private static JSONObject user;

    private static Long categoryId;

    private static JSONObject category;

    private static Long appId;

    private static JSONObject app;

    private static String hawkbitAddress;

    private static Credentials appstoreCredentials;

    @PostConstruct
    @BeforeClass
    public static void integrationSetup() throws Exception {
        System.out.println("SETUP START");

        deviceId = globalConfig.getDeviceId();

        hawkbitAddress = hawkbitConfig.getAddress();
        hawkbitCredentials = new Credentials(hawkbitConfig.getUsername(), hawkbitConfig.getPassword());

        appstoreAddress = appstoreConfig.getAddress();
        appstoreCredentials = new Credentials(appstoreConfig.getUsername(), appstoreConfig.getPassword());

        System.out.println("CREATING APP");

        user = new JSONObject(createUser());
        userId = user.getLong(JSON_PROPERTY_ID);

        category = new JSONObject(createCategory());
        categoryId = category.getLong(JSON_PROPERTY_ID);

        app = new JSONObject(createApp(categoryId));
        appId = app.getLong(JSON_PROPERTY_ID) ;

        System.out.println("userId " + userId + " categoryId " + categoryId + " appId " + appId);
        addAppToUser();

        System.out.println("SETTING UP HAWKBIT");

        String responseBody = createTarget();
        JSONArray jsonArray = new JSONArray(responseBody);
        String element = jsonArray.getString(0);
        JSONObject body = new JSONObject(element);

        controllerId = body.getString("controllerId");
        securityToken = body.getString("securityToken");

        System.out.println(body);
    }

    @AfterClass
    public static void integrationCleanup() {
        System.out.println("CLEANUP START");

        if (appId != null) {
            removeApp(appId);
            removeUser(userId);
            removeCategory(categoryId);
        }
    }

    private static void addAppToUser() throws Exception {

        Request requestApp = getBaseRequestBuilder()
                .put()
                .url(buildUrl(appstoreAddress, "/api/1.0/app/" + appId))
                .body(new JSONObject(app.toString())
                        .put("installedusers", new JSONArray().put(user))
                )
                .build();

        ResponseEntity<String> response2 = executeApiCall(requestApp);

        if (!response2.getStatusCode().is2xxSuccessful()) {
            fail("Failed to update app.");
        }

    }

    @Override
    protected String getTestFile() {
        return "SmallIntegration-TestSuite.yaml";
    }

    @Test
    public void testGetTenantInfo() {
        // GIVEN
        ResponseResult result = testCase.getResult(0);

        Request request = new Request.Builder()
                .url(buildUrl(hawkbitConfig.getAddress(), "/" + hawkbitConfig.getTenant() + "/controller/v1/" + deviceId))
                .get()
                .headers(getBaseRequestHeaders())
                .addHeader("Authorization", "TargetToken " + securityToken)
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        assertEquals(result.getStatusCode(), responseEntity.getStatusCodeValue());

        String body = responseEntity.getBody();
        assertNotNull(body);
    }

    @Test
    public void testGetSoftwareModuleArtifacts() throws JSONException {
        // GIVEN
        ResponseResult result = testCase.getResult(0);

        Request request = new Request.Builder()
                .url(buildUrl(hawkbitConfig.getAddress(), "/" + hawkbitConfig.getTenant() + "/controller/v1/" + deviceId + "/softwaremodules/" + appId + "/artifacts"))
                .get()
                .headers(getBaseRequestHeaders())
                .addHeader("Authorization", "TargetToken " + securityToken)
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        assertEquals(result.getStatusCode(), responseEntity.getStatusCodeValue());

        String body = responseEntity.getBody();
        assertNotNull(body);
    }

    @Test
    public void createSoftwareModule() throws JSONException {
        // GIVEN

        Request request = new Request.Builder()
                .url(buildUrl(hawkbitConfig.getAddress(), "/" + hawkbitConfig.getTenant() + "/controller/v1/" + deviceId + "/softwaremodules"))
                .post()
                .headers(getBaseRequestHeaders())
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        System.out.println(responseEntity.getBody());
    }

    private static String createTarget() throws JSONException {
        // build request
        String controllerId = deviceId;
        String name = "hawkbit.tester";
        String description = "test target";

        JSONArray requestBody = new JSONArray()
                .put(
                        new JSONObject()
                                .put("controllerId", controllerId)
                                .put("name", name)
                                .put("description", description)
                );

        Request request = new Request.Builder()
                .url(buildUrl(appstoreAddress, "/rest/v1/targets"))
                .post()
                .headers(getBaseRequestHeaders())
                .body(requestBody)
                .credentials(hawkbitCredentials)
                .build();

        // execute request
        ResponseEntity<String> responseEntity = executeApiCall(request);

        if (responseEntity.getStatusCodeValue() != HttpStatus.CREATED.value()) {
            fail("Failed to create target.");
        }

        return responseEntity.getBody();
    }

    protected static Request.Builder getBaseRequestBuilder() {
        return new Request.Builder()
                .headers(getBaseRequestHeaders())
                .credentials(hawkbitCredentials);
    }

    protected static String createUser() throws JSONException {
        Request request = getBaseRequestBuilder()
                .post()
                .url(buildUrl(appstoreAddress, "/api/1.0/user/"))
                .body(new JSONObject()
                        .put(JSON_PROPERTY_USER_ADMINUSER, JSON_PROPERTY_USER_ADMINUSER_VALUE)
                        .put(JSON_PROPERTY_USER_USERNAME, JSON_PROPERTY_USER_USERNAME_VALUE)
                        .put(JSON_PROPERTY_USER_PASSWORD, JSON_PROPERTY_USER_PASSWORD_VALUE)
                        .put(JSON_PROPERTY_USER_USERTYPE, JSON_PROPERTY_USER_USERTYPE_VALUE)
                )
                .build();

        ResponseEntity<String> response = executeApiCall(request);
        LOGGER.info("JUST CREATED " +  response.getBody());

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to create app store user."));
        }

        return response.getBody();
    }

    protected static void removeUser(Long userId) {
        Request request = getBaseRequestBuilder()
                .delete()
                .url(buildUrl(appstoreAddress, "/api/1.0/user/" + userId))
                .build();

        ResponseEntity<String> response = executeApiCall(request);

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to remove app store user."));
        }
    }

    protected static String createCategory() throws JSONException {
        Request request = getBaseRequestBuilder()
                .post()
                .url(buildUrl(appstoreAddress, "/api/1.0/appcategory/"))
                .body(new JSONObject()
                        .put(JSON_PROPERTY_CATEGORY_NAME, JSON_PROPERTY_CATEGORY_NAME_VALUE)
                )
                .build();

        ResponseEntity<String> response = executeApiCall(request);
        LOGGER.log(Level.INFO, "JUST CREATED " +  response.getBody());

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to create app store category."));
        }

        return response.getBody();
    }

    protected static void removeCategory(Long categoryId) {
        Request request = getBaseRequestBuilder()
                .delete()
                .url(buildUrl(appstoreAddress, "/api/1.0/appcategory/" + categoryId))
                .build();

        ResponseEntity<String> response = executeApiCall(request);

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to remove app store category."));
        }
    }

    protected static String createApp(Long categoryId) throws JSONException {
        try {
            JSON_PROPERTY_APP_CATEGORY_NAME_VALUE = new JSONObject().put("id", categoryId ).put("name", "test.app.category");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println(JSON_PROPERTY_APP_CATEGORY_NAME_VALUE);

        Request request = getBaseRequestBuilder()
                .post()
                .url(buildUrl(appstoreAddress, "/api/1.0/app/"))
                .body(new JSONObject()
                        .put(JSON_PROPERTY_APP_NAME, JSON_PROPERTY_APP_NAME_VALUE)
                        .put(JSON_PROPERTY_APP_VERSION, JSON_PROPERTY_APP_VERSION_VALUE)
                        .put(JSON_PROPERTY_APP_DESCRIPTION, JSON_PROPERTY_APP_DESCRIPTION_VALUE)
                        .put(JSON_PROPERTY_APP_HAWKBIT_NAME, JSON_PROPERTY_APP_HAWKBIT_NAME_VALUE)
                        .put(JSON_PROPERTY_APP_OWNER, JSON_PROPERTY_APP_OWNER_VALUE )
                        .put(JSON_PROPERTY_APP_CATEGORY_NAME, JSON_PROPERTY_APP_CATEGORY_NAME_VALUE )
                        .put(JSON_PROPERTY_APP_DOWNLOADCOUNT, JSON_PROPERTY_DOWNLOADCOUNT_VALUE )
                        .put(JSON_PROPERTY_APP_PUBLISH_DATE, JSON_PROPERTY_APP_PUBLISH_DATE_VALUE)
                )
                .build();

        ResponseEntity<String> response = executeApiCall(request);
        System.out.println("JUST CREATED " +  response.getBody());

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to create app store app."));
        }
        return response.getBody();
    }

    protected static String removeApp(Long appId) {
        Request request = getBaseRequestBuilder()
                .delete()
                .url(buildUrl(appstoreAddress, "/api/1.0/app/" + appId))
                .build();

        ResponseEntity<String> response = executeApiCall(request);

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to remove app store app."));
        }

        return response.getBody();
    }


}
