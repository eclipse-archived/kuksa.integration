package org.eclipse.kuksa.testing;

import org.eclipse.kuksa.testing.client.Request;
import com.assystem.kuksa.testing.config.GlobalConfiguration;
import com.assystem.kuksa.testing.config.HawkBitConfiguration;
import org.eclipse.kuksa.testing.model.Credentials;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.*;

/**
 * Collection of test concerning the HawkBit Target API. </br>
 * <link>https://www.eclipse.org/hawkbit/apis/mgmt/targets/</link>
 *
 * @author cnguyen
 */
@ContextConfiguration(classes = {GlobalConfiguration.class, HawkBitConfiguration.class})
public class HawkBitTargetApiTest extends AbstractTestCase {

    @Autowired
    private HawkBitConfiguration config;

    private Credentials credentials;

    private String address;

    private String controllerId;

    @Override
    protected String getTestFile() {
        return "HawkBitTargetApi-TestSuite.yaml";
    }

    @Override
    protected void testSetup() throws JSONException {
        address = config.getAddress();
        credentials = new Credentials(config.getUsername(), config.getPassword());

        String responseBody = createTarget();
        JSONArray jsonArray = new JSONArray(responseBody);
        String element = jsonArray.getString(0);
        JSONObject body = new JSONObject(element);

        controllerId = body.getString("controllerId");
    }

    private String createTarget() throws JSONException {
        // build request
        String controllerId = "kuksa.testing.hawkbit";
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
                .url(buildUrl(PROTOCOL_HTTP, address, "/rest/v1/targets"))
                .post()
                .headers(getBaseRequestHeaders())
                .body(requestBody)
                .credentials(credentials)
                .build();

        // execute request
        ResponseEntity<String> responseEntity = executeApiCall(request);

        if (responseEntity.getStatusCodeValue() != HttpStatus.CREATED.value()) {
            fail("Failed to create target.");
        }

        return responseEntity.getBody();
    }

    @Override
    protected void testCleanup() {
        if (controllerId != null) {
            removeTargetForCleanup(controllerId);
        }
    }

    private void removeTargetForCleanup(String controllerId) {
        // build request
        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, address, "/rest/v1/targets/" + controllerId))
                .delete()
                .headers(getBaseRequestHeaders())
                .credentials(credentials)
                .build();

        // execute request
        ResponseEntity<String> responseEntity = executeApiCall(request);

        if (responseEntity.getStatusCodeValue() != HttpStatus.OK.value()) {
            fail("Failed to remove target.");
        }
    }

    @Test
    public void testCreateTarget() throws JSONException {
        // GIVEN
        String url = buildUrl(PROTOCOL_HTTP, address, "/rest/v1/targets");

        String controllerId = "test.create.target";
        String name = "test.target";
        String description = "test target";

        JSONArray requestBody = new JSONArray()
                .put(
                        new JSONObject()
                                .put("controllerId", controllerId)
                                .put("name", name)
                                .put("description", description)
                );

        Request request = new Request.Builder()
                .url(url)
                .post()
                .headers(getBaseRequestHeaders())
                .body(requestBody)
                .credentials(credentials)
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        JSONArray jsonArray = new JSONArray(responseEntity.getBody());
        assertNotNull(jsonArray);

        String element = jsonArray.getString(0);
        assertNotNull(element);

        JSONObject body = new JSONObject(element);
        assertNotNull(body);

        assertEquals(config.getUsername(), body.get("createdBy"));
        assertEquals(name, body.get("name"));
        assertEquals(controllerId, body.get("controllerId"));
        assertEquals(description, body.get("description"));
        assertNotNull(body.get("securityToken"));

        // remove target to avoid conflict for future purpose
        removeTargetForCleanup(controllerId);
    }

    @Test
    public void testGetAllTargets() throws JSONException {
        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, address, "/rest/v1/targets"))
                .get()
                .headers(getBaseRequestHeaders())
                .credentials(credentials)
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        JSONObject body = new JSONObject(responseEntity.getBody());
        assertNotNull(body);

        assertEquals(14, body.get("total"));
    }

    @Test
    public void testDeleteTarget() {
        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, address, "/rest/v1/targets/" + controllerId))
                .delete()
                .headers(getBaseRequestHeaders())
                .credentials(credentials)
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // remove target to avoid conflict for future purpose
        controllerId = null;
    }

    @Test
    public void testGetTarget() throws JSONException {
        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, address, "/rest/v1/targets/" + controllerId))
                .get()
                .headers(getBaseRequestHeaders())
                .credentials(credentials)
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        JSONObject body = new JSONObject(responseEntity.getBody());
        assertNotNull(body);

        assertEquals(controllerId, body.get("controllerId"));
    }

    @Test
    public void testEditTarget() throws JSONException {
        // GIVEN
        String value = "test.edit.target";

        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, address, "/rest/v1/targets/" + controllerId))
                .put()
                .credentials(credentials)
                .headers(getBaseRequestHeaders())
                .body(new JSONObject()
                        .put("name", value)
                        .put("description", value)
                )
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        JSONObject body = new JSONObject(responseEntity.getBody());
        assertNotNull(body);

        assertEquals(value, body.get("name"));
        assertEquals(value, body.get("description"));
    }

    @Test
    public void testGetTargetActions() throws JSONException {
        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, address, "/rest/v1/targets/" + controllerId + "/actions"))
                .get()
                .credentials(credentials)
                .headers(getBaseRequestHeaders())
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        JSONObject body = new JSONObject(responseEntity.getBody());
        assertNotNull(body);
    }

}
