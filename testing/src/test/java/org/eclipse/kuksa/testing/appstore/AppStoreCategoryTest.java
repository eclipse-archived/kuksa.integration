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

public class AppStoreCategoryTest extends AbstractAppStoreTest {

    private static final String JSON_PROPERTY_ID = "id";

    private Long categoryId;

    @Override
    protected String getTestFile() {
        return "AppStore-AppCategory-TestSuite.yaml";
    }

    @Override
    protected void testSetup() throws Exception {
        super.testSetup();
        String body = createCategory();
        JSONObject jsonObject = new JSONObject(body);
        categoryId = jsonObject.getLong(JSON_PROPERTY_ID);
    }

    @Override
    protected void testCleanup() throws Exception {
        super.testCleanup();
        if (categoryId != null) {
            removeCategory(categoryId);
        }
    }

    @Test
    public void testCreateAppCategory() throws Exception {
        // GIVEN
        ResponseResult result = testCase.getResult(0);

        String category = "appcategory";

        Request request = getBaseRequestBuilder()
                .post()
                .url(buildUrl(address, "/api/1.0/appcategory/"))
                .body(new JSONObject()
                        .put(JSON_PROPERTY_CATEGORY_NAME, category)
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
        assertEquals(category, jsonObject.getString(JSON_PROPERTY_CATEGORY_NAME));

        Long id = jsonObject.getLong(JSON_PROPERTY_ID);
        assertNotNull(id);

        // remove newly created category to avoid future conflicts
        removeCategory(id);
    }

    @Test
    public void testGetAppCategory() throws Exception {
        // GIVEN
        ResponseResult result = testCase.getResult(0);

        Request request = getBaseRequestBuilder()
                .get()
                .url(buildUrl(address, "/api/1.0/appcategory/" + categoryId))
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        assertNotNull(response);
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());

        String body = response.getBody();
        assertNotNull(body);

        JSONObject jsonObject = new JSONObject(body);
        assertEquals(categoryId.longValue(), jsonObject.getLong(JSON_PROPERTY_ID));
    }

    @Test
    public void testUpdateAppCategory() throws Exception {
        // GIVEN
        ResponseResult result = testCase.getResult(0);

        String newCategoryName = "new.category.name";

        Request request = getBaseRequestBuilder()
                .put()
                .url(buildUrl(address, "/api/1.0/appcategory/" + categoryId))
                .body(new JSONObject()
                        .put(JSON_PROPERTY_CATEGORY_NAME, newCategoryName)
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
        assertEquals(categoryId.longValue(), jsonObject.getLong(JSON_PROPERTY_ID));
        assertEquals(newCategoryName, jsonObject.getString(JSON_PROPERTY_CATEGORY_NAME));
    }

    @Test
    public void testRemoveAppCategory() {
        // GIVEN
        ResponseResult result = testCase.getResult(0);

        Request request = getBaseRequestBuilder()
                .delete()
                .url(buildUrl(address, "/api/1.0/appcategory/" + categoryId))
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        assertNotNull(response);
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());

        // avoid cleanup
        categoryId = null;
    }

    @Test
    public void testGetAllAppCategories() throws Exception {
        // GIVEN
        ResponseResult result = testCase.getResult(0);

        Request request = getBaseRequestBuilder()
                .get()
                .url(buildUrl(address, "/api/1.0/appcategory"))
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        assertNotNull(response);
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());

        JSONObject body = new JSONObject(response.getBody());
        assertNotNull(body);
    }

}
