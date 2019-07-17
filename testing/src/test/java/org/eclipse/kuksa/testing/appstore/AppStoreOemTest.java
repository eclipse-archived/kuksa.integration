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

package org.eclipse.kuksa.testing.appstore;

import org.eclipse.kuksa.testing.client.Request;
import org.eclipse.kuksa.testing.model.ResponseResult;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.*;

public class AppStoreOemTest extends AbstractAppStoreTest {

    private static final String JSON_PROPERTY_ID = "id";

    private static final String JSON_PROPERTY_NAME = "name";
    private static final String JSON_PROPERTY_NAME_VALUE = "OEM_TEST";

    private Long id;

    @Override
    protected String getTestFile() {
        return "AppStore-Oem-TestSuite.yaml";
    }

    @Override
    protected void testCleanup() throws Exception {
        super.testCleanup();
        if (id != null) {
            removeOEM(id);
        }
    }

    private String createOem() throws JSONException {
        Request request = getBaseRequestBuilder()
                .post()
                .url(buildUrl(address, "/api/1.0/oem/"))
                .body(new JSONObject()
                        .put(JSON_PROPERTY_NAME, JSON_PROPERTY_NAME_VALUE)
                )
                .build();

        ResponseEntity<String> response = executeApiCall(request);

        if (!response.getStatusCode().is2xxSuccessful()) {
            fail("Failed to create app store oem.");
        }

        return response.getBody();
    }

    @Test
    public void testCreateOem() throws Exception {
        // GIVEN
        ResponseResult result = testCase.getResult(0);

        String oem = "OEM_TEST_EXPLICIT";

        Request request = getBaseRequestBuilder()
                .post()
                .url(buildUrl(address, "/api/1.0/oem/"))
                .body(new JSONObject()
                        .put(JSON_PROPERTY_NAME, oem)
                )
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        assertNotNull(response);
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());

        String body = response.getBody();
        assertNotNull(body);

        JSONObject jsonObject = new JSONObject(body);
        assertEquals(oem, jsonObject.getString(JSON_PROPERTY_NAME));

        id = jsonObject.getLong(JSON_PROPERTY_ID);
        assertNotNull(id);
    }

    @Test
    public void testGetOem() throws Exception {
        // GIVEN
        ResponseResult result = testCase.getResult(0);

        Long oemId = new JSONObject(createOem())
                .getLong(JSON_PROPERTY_ID);

        System.out.println("Getting oem for user " + oemId);

        Request request = getBaseRequestBuilder()
                .get()
                .url(buildUrl(address, "/api/1.0/oem/" + oemId))
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        assertNotNull(response);
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());

        String body = response.getBody();
        assertNotNull(body);

        JSONObject jsonObject = new JSONObject(body);
        assertEquals(JSON_PROPERTY_NAME_VALUE, jsonObject.getString(JSON_PROPERTY_NAME));

        id = jsonObject.getLong(JSON_PROPERTY_ID);
        assertNotNull(id);
        assertEquals(oemId, id);
    }

}
