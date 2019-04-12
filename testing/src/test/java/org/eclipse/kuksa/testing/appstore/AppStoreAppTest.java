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

package  org.eclipse.kuksa.testing.appstore;

import org.eclipse.kuksa.testing.client.Request;
import org.eclipse.kuksa.testing.model.ResponseResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.util.Date;

import static org.junit.Assert.*;



public class AppStoreAppTest extends AbstractAppStoreTest {

    private static final String JSON_PROPERTY_ID = "id";

    private Long userId;

    private JSONObject user;

    private Long categoryId;

    private JSONObject category;

    private Long appId;

    private JSONObject app;

    @Override
    protected String getTestFile() {
        return "AppStore-App-TestSuite.yaml";
    }

    @Override
    protected void testSetup() throws Exception {
        super.testSetup();

        user = new JSONObject(createUser());
        userId = user.getLong(JSON_PROPERTY_ID);

        category = new JSONObject(createCategory());
        categoryId = category.getLong(JSON_PROPERTY_ID);

        app = new JSONObject(createApp(categoryId));
        appId = app.getLong(JSON_PROPERTY_ID) ;

        System.out.println("userId " + userId + " categoryId " + categoryId + " appId " + appId);
        addAppToUser();
    }


    private void addAppToUser() throws Exception {

        Request requestApp = getBaseRequestBuilder()
                .put()
                .url(buildUrl(address, "/api/1.0/app/" + appId))
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
    protected void testCleanup() throws Exception {
        super.testCleanup();
        if (appId != null) {
            removeApp(appId);
            removeUser(userId);
            removeCategory(categoryId);
        }
    }

    @Test
    public void testCreateApp() throws Exception {
        // GIVEN
        ResponseResult result = testCase.getResult(0);

        String name = "test.app.explicit";
        String version = "1.0";
        String description = "test.app.explicit.description";
        JSONObject appcategory = new JSONObject().put("id", categoryId).put("name", category.getString("name"));
        String hawkBitName = "test.app.explicit.hawkbit.name";
        String owner = "test.app.explicit.owner";
        long publishDate = new Date().getTime();

        Request request = getBaseRequestBuilder()
                .post()
                .url(buildUrl(address, "/api/1.0/app/"))
                .body(new JSONObject()
                        .put(JSON_PROPERTY_APP_NAME, name)
                        .put(JSON_PROPERTY_APP_VERSION, version)
                        .put(JSON_PROPERTY_APP_DESCRIPTION, description)
                        .put(JSON_PROPERTY_APP_HAWKBIT_NAME, hawkBitName)
                        .put(JSON_PROPERTY_APP_CATEGORY_NAME, appcategory)
                        .put(JSON_PROPERTY_APP_OWNER, owner)
                        .put(JSON_PROPERTY_APP_DOWNLOADCOUNT, 0 )
                        .put(JSON_PROPERTY_APP_PUBLISH_DATE, publishDate)
                )
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);
        System.out.println("JUST CREATED " +  response.getBody());

        // THEN
        assertNotNull(response);
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());

        JSONObject body = new JSONObject(response.getBody());
        assertNotNull(body);
        assertEquals(name, body.getString(JSON_PROPERTY_APP_NAME));
        assertEquals(version, body.getString(JSON_PROPERTY_APP_VERSION));
        assertEquals(description, body.getString(JSON_PROPERTY_APP_DESCRIPTION));
        assertEquals(owner, body.getString(JSON_PROPERTY_APP_OWNER));

        Long id = body.getLong(JSON_PROPERTY_ID);
        assertNotNull(id);

        // avoid future conflict
        removeApp(id);
    }

    @Test
    public void testGetApp() throws Exception {
        // GIVEN
        ResponseResult result = testCase.getResult(0);

        Request request = getBaseRequestBuilder()
                .get()
                .url(buildUrl(address, "/api/1.0/app/" + appId))
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        assertNotNull(response);
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());

        JSONObject body = new JSONObject(response.getBody());
        assertNotNull(body);
        assertEquals(appId.longValue(), body.getLong(JSON_PROPERTY_ID));
    }

    @Test
    public void testGetAllApps() throws Exception {
        // GIVEN
        ResponseResult result = testCase.getResult(0);

        Request request = getBaseRequestBuilder()
                .get()
                .url(buildUrl(address, "/api/1.0/app"))
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        assertNotNull(response);
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());

        JSONObject body = new JSONObject(response.getBody());
        assertNotNull(body);
    }

    @Test
    public void testUpdateApp() throws Exception {
        // GIVEN
        ResponseResult result = testCase.getResult(0);

        String newDescription = "new.app.description";

        Request request = getBaseRequestBuilder()
                .put()
                .url(buildUrl(address, "/api/1.0/app/" + appId))
                .body(new JSONObject(app.toString())
                        .put(JSON_PROPERTY_APP_DESCRIPTION, newDescription)
                )
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        assertNotNull(response);
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());

        JSONObject body = new JSONObject(response.getBody());
        assertNotNull(body);
        assertEquals(JSON_PROPERTY_APP_NAME_VALUE, body.getString(JSON_PROPERTY_APP_NAME));
        assertEquals(JSON_PROPERTY_APP_VERSION_VALUE, body.getString(JSON_PROPERTY_APP_VERSION));
        assertEquals(newDescription, body.getString(JSON_PROPERTY_APP_DESCRIPTION));
        assertEquals(JSON_PROPERTY_APP_OWNER_VALUE, body.getString(JSON_PROPERTY_APP_OWNER));
    }

    @Test
    public void testRemoveApp() {
        // GIVEN
        ResponseResult result = testCase.getResult(0);

        Request request = getBaseRequestBuilder()
                .delete()
                .url(buildUrl(address, "/api/1.0/app/" + appId))
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        assertNotNull(response);
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());
    }

    @Test
    public void testGetUserInstalledApp() throws Exception {

        // GIVEN
        ResponseResult result = testCase.getResult(0);

        Request request = getBaseRequestBuilder()
                .get()
                .url(buildUrl(address, "/api/1.0/app/installed/user/" + userId))
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        assertNotNull(response);
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());

        JSONObject body = new JSONObject(response.getBody());
        assertNotNull(body);
        System.out.println(body.toString());
        assertEquals(1, body.getInt("totalElements"));
    }

    @Test
    public void testGetAppInstalledUsers() throws Exception {

        // GIVEN

        Request request = getBaseRequestBuilder()
                .get()
                .url(buildUrl(address, "/api/1.0/app/" + appId))
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        assertNotNull(response);

        JSONObject body = new JSONObject(response.getBody());
        assertNotNull(body);
        assertEquals(1, body.getJSONArray("installedusers").length());
    }

    @Test
    public void testGetUserInstalledAppsWithText() throws Exception {
        // GIVEN
        ResponseResult result = testCase.getResult(0);

        String appName = app.getString(JSON_PROPERTY_APP_NAME);

        Request request = getBaseRequestBuilder()
                .get()
                .url(buildUrl(address, "/api/1.0/app/installed/user/" + userId + "/" + appName))
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        assertNotNull(response);
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());

        JSONObject body = new JSONObject(response.getBody());
        assertNotNull(body);
        assertEquals(1, body.getInt("totalElements"));
    }

    @Test
    public void testGetAppsByCategory() throws Exception {
        // GIVEN
        ResponseResult result = testCase.getResult(0);

        Request request = getBaseRequestBuilder()
                .get()
                .url(buildUrl(address, "/api/1.0/app/category/" + categoryId))
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        assertNotNull(response);
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());

        JSONObject body = new JSONObject(response.getBody());
        assertNotNull(body);
        assertEquals(1, body.getInt("totalElements"));
    }

    @Test
    public void testGetAppsByCategoryAndAppName() throws Exception {
        // GIVEN
        ResponseResult result = testCase.getResult(0);

        String appName = app.getString(JSON_PROPERTY_APP_NAME);

        Request request = getBaseRequestBuilder()
                .get()
                .url(buildUrl(address, "/api/1.0/app/category/" + categoryId + "/" + appName))
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        assertNotNull(response);
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());

        JSONObject body = new JSONObject(response.getBody());
        assertNotNull(body);
        assertEquals(1, body.getInt("totalElements"));
    }

    @Test
    public void testPurchaseApp() throws Exception {
        // GIVEN
        ResponseResult result = testCase.getResult(0);

        Request request = getBaseRequestBuilder()
                .post()
                .url(buildUrl(address, "/api/1.0/app/" + appId + "/purchase/" + userId))
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        assertNotNull(response);
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());
//
//        JSONObject body = new JSONObject(response.getBody());
//        assertNotNull(body);
//
//        JSONArray owners = body.getJSONArray("ownerusers");
//        assertNotNull(owners);
//        assertEquals(1, owners.length());
    }

}
