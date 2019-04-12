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
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AppStoreUserTest extends AbstractAppStoreTest {

    private Long userId;

    @Override
    protected String getTestFile() {
        return "AppStore-User-TestSuite.yaml";
    }

    @Override
    protected void testSetup() throws Exception {
        super.testSetup();
        String body = createUser();
        JSONObject jsonObject = new JSONObject(body);
        System.out.println("USER" + body);
        userId = jsonObject.getLong("id");
    }

    @Override
    protected void testCleanup() {
        if (userId != null) {
            removeUser(userId);
        }
    }

    @Test
    public void testCreateUser() throws Exception {
        // GIVEN
        String username = "test.create.user";
        String password = "test.create.user.password";

        Request request = getBaseRequestBuilder()
                .post()
                .url(buildUrl(address, "/api/1.0/user/"))
                .body(new JSONObject()
                        .put("adminuser", false)
                        .put(JSON_PROPERTY_USER_USERNAME, username)
                        .put(JSON_PROPERTY_USER_PASSWORD, password)
                        .put(JSON_PROPERTY_USER_USERTYPE, JSON_PROPERTY_USER_USERTYPE_VALUE)
                )
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        ResponseResult result = testCase.getResult(0);

        assertNotNull(response);
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());

        String body = response.getBody();
        assertNotNull(body);

        JSONObject jsonObject = new JSONObject(body);
        assertEquals(username, jsonObject.getString("username"));
        assertEquals(password, jsonObject.getString("password"));
        removeUser(jsonObject.getLong("id"));
    }

    @Test
    public void testGetUser() throws Exception {
        // GIVEN
        Request request = getBaseRequestBuilder()
                .get()
                .url(buildUrl(address, "/api/1.0/user/" + userId))
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        ResponseResult result = testCase.getResult(0);

        assertNotNull(response);
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());

        String body = response.getBody();
        assertNotNull(body);

        JSONObject jsonObject = new JSONObject(body);
        assertEquals(String.valueOf(userId), jsonObject.getString("id"));
    }

    @Test
    public void testUpdateUser() throws Exception {
        // GIVEN
        String newUsername = "new.username";
        String newPassword = "new.password";

        Request request = getBaseRequestBuilder()
                .put()
                .url(buildUrl(address, "/api/1.0/user/" + userId))
                .body(new JSONObject()
                        .put(JSON_PROPERTY_USER_USERNAME, newUsername)
                        .put(JSON_PROPERTY_USER_PASSWORD, newPassword)
                        .put(JSON_PROPERTY_USER_USERTYPE, JSON_PROPERTY_USER_USERTYPE_VALUE)
                )
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        ResponseResult result = testCase.getResult(0);

        assertNotNull(response);
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());

        String body = response.getBody();
        assertNotNull(body);

        JSONObject jsonObject = new JSONObject(body);
        assertEquals(Long.valueOf(userId).longValue(), jsonObject.getLong("id"));
    }

    @Test
    public void testDeleteUser() {
        // GIVEN
        Request request = getBaseRequestBuilder()
                .delete()
                .url(buildUrl(address, "/api/1.0/user/" + userId))
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        ResponseResult result = testCase.getResult(0);

        assertNotNull(response);
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());

        // to avoid conflict with cleanup
        userId = null;
    }

    @Test
    public void testGetAllUser() {
        // GIVEN
        Request request = getBaseRequestBuilder()
                .get()
                .url(buildUrl(address, "/api/1.0/user"))
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        ResponseResult result = testCase.getResult(0);

        assertNotNull(response);
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());
    }

    @Test
    public void testValidateUser() throws Exception {
        // GIVEN
        Request request = getBaseRequestBuilder()
                .post()
                .url(buildUrl(address, "/api/1.0/user/validation"))
                .body(new JSONObject()
                        .put("adminuser", false)
                        .put(JSON_PROPERTY_USER_USERNAME, JSON_PROPERTY_USER_USERNAME_VALUE)
                        .put(JSON_PROPERTY_USER_PASSWORD, JSON_PROPERTY_USER_PASSWORD_VALUE)
                )
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        ResponseResult result = testCase.getResult(0);

        assertNotNull(response);
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());

        String body = response.getBody();
        assertNotNull(body);

        JSONObject jsonObject = new JSONObject(body);
        assertEquals(String.valueOf(userId), jsonObject.getString("id"));
    }

}
