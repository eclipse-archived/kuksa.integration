package org.eclipse.kuksa.testing;

import org.eclipse.kuksa.testing.client.Request;
import com.assystem.kuksa.testing.config.GlobalConfiguration;
import com.assystem.kuksa.testing.config.HawkBitConfiguration;
import org.eclipse.kuksa.testing.model.ResponseResult;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@ContextConfiguration(classes = {GlobalConfiguration.class, HawkBitConfiguration.class})
public class HawkBitTest extends AbstractTestCase {

    @Autowired
    private GlobalConfiguration globalConfig;

    @Autowired
    private HawkBitConfiguration config;

    private String securityToken;

    private String deviceId;

    @Before
    public void setup() {
        securityToken = globalConfig.getSecurityToken();
        deviceId = globalConfig.getDeviceId();
    }

    @Override
    protected String getTestFile() {
        return "HawkBit-TestSuite.yaml";
    }

    @Test
    public void testGetTenantInfo() {
        // GIVEN
        ResponseResult result = testCase.getResult(0);

        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getAddress(), "/" + config.getTenant() + "/controller/v1/" + deviceId))
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
        assertEquals(result.getBody(), body);
    }

    @Test
    public void testGetSoftwareModuleArtifacts() throws JSONException {
        // GIVEN
        ResponseResult result = testCase.getResult(0);

        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getAddress(), "/" + config.getTenant() + "/controller/v1/" + deviceId + "/softwaremodules/22/artifacts"))
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
        assertEquals(result.getBody(), body);
    }

}
