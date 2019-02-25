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

package org.eclipse.kuksa.testing;

import org.eclipse.kuksa.testing.client.Request;
import org.eclipse.kuksa.testing.config.GlobalConfiguration;
import org.eclipse.kuksa.testing.config.HonoConfiguration;
import org.eclipse.kuksa.testing.model.Credentials;
import org.eclipse.kuksa.testing.model.ResponseResult;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@ContextConfiguration(classes = {GlobalConfiguration.class, HonoConfiguration.class})
public class HonoApiTest extends AbstractTestCase {

    private static final String PATH_TENANT = "/tenant/";

    private static final String PATH_REGISTRATION = "/registration/";

    private static final String PATH_CONTROL = "/rover/1/RoverDriving/control/";

    private static final String PATH_TELEMETRY = "rover/1/telemetry";

    private static final String PATH_CREDENTIALS = "/credentials/";


    @Autowired
    private GlobalConfiguration globalConfig;

    @Autowired
    private HonoConfiguration config;

    private String deviceId;

    @Before
    public void setup() {
        deviceId = globalConfig.getDeviceId();
    }

    @Override
    protected String getTestFile() {
        return "Hono-TestSuite.yaml";
    }

    @Test
    public void testGetTenantInfo() {
        // GIVEN
        String tenantId = "DEFAULT_TENANT";

        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getDeviceRegistryStable(), PATH_TENANT + tenantId))
                .get()
                .headers(getBaseRequestHeaders())
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        ResponseResult result = testCase.getResult(0);

        assertEquals(result.getStatusCode(), responseEntity.getStatusCodeValue());

        JSONObject body = getBodyAsJson(responseEntity);
        assertNotNull(body);

        assertEquals(result.getBody(), body.toString());

        assertEquals(tenantId, getJsonValue(body, "tenant-id"));

        // {"tenant-id":"DEFAULT_TENANT","enabled":true}
    }

    @Test
    public void testGetDeviceInfo() {
        // GIVEN
        String deviceId = "4711";

        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getDeviceRegistryStable(), PATH_REGISTRATION + "DEFAULT_TENANT/" + deviceId))
                .get()
                .headers(getBaseRequestHeaders())
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        JSONObject body = getBodyAsJson(responseEntity);
        assertNotNull(body);

        assertEquals(deviceId, getJsonValue(body, "device-id"));
        // {"data":{"enabled":true},"device-id":"4711"}
    }

    /*
     * TODO FOR EACH POST-REQUEST WE NEED A ROLLBACK-FUNCTION -> the test will fail after one successful run
     */

    public void testPostTenantInfo() throws JSONException {
        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getDeviceRegistryStable(), PATH_TENANT))
                .post()
                .headers(getBaseRequestHeaders())
                .body(new JSONObject()
                        .put("tenant-id", "ASSYSTEM_TENANT4"))
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());

        JSONObject body = getBodyAsJson(responseEntity);
        assertNotNull(body);
    }

    @Test
    public void testPostDeviceInfo() throws JSONException {
        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getDeviceRegistryStable(), PATH_REGISTRATION + "ASSYSTEM_TENANT4"))
                .post()
                .headers(getBaseRequestHeaders())
                .body(new JSONObject()
                        .put("device-id", "assystem2"))
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

 //       JSONObject body = getBodyAsJson(responseEntity);
 //       assertNotNull(body);
    }

//    @Test
    public void testPostTelemetryData() throws JSONException {
        // GIVEN
        String username = "1@ASSYSTEM_TENANT4";
        String password = "hashdis";

        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getAdapterHttpVertxStable(), PATH_TELEMETRY))
                .post()
                .headers(getBaseRequestHeaders())
                .body(new JSONObject()
                        .put("temperature", "5"))
                .credentials(new Credentials(username, password))
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

//		JSONObject body = getBodyAsJson(response);
//		assertNotNull(body);
    }

//    @Test
    public void testPostEventData() throws JSONException {
        // GIVEN
        String username = "1@ASSYSTEM_TENANT4";
        String password = "hashdis";

        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getAdapterHttpVertxStable(), PATH_TELEMETRY))
                .post()
                .headers(getBaseRequestHeaders())
                .body(new JSONObject()
                        .put("event", "snow"))
                .credentials(new Credentials(username, password))
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        // THEN
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

//		JSONObject body = getBodyAsJson(response);
//		assertNotNull(body);
    }



    @Test
    public void testPublishControlData() throws Exception {
        // publish
        String publisherId = UUID.randomUUID().toString();
        try {
            IMqttClient client = new MqttClient(PROTOCOL_TCP + config.getAdapterMqttVertxStable(),publisherId);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
        //    options.setUserName("assystem1@ASSYSTEM_TENANT4");
        //    options.setPassword("whySoSecret".toCharArray());
            options.setConnectionTimeout(10);
            client.connect(options);

            // receive
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    LOGGER.info("MQTT Error - Connection lost", throwable);
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    LOGGER.info("MQTT Message arrived successfully: " + mqttMessage);
                    assertNotNull(mqttMessage.getPayload());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    LOGGER.info("MQTT Message delivery completed " + iMqttDeliveryToken);
                    assertEquals(true, iMqttDeliveryToken.isComplete());
                }
            });
            CountDownLatch receivedSignal = new CountDownLatch(10);
            client.subscribe(PATH_TELEMETRY, (topic, msg) -> {
                byte[] payload = msg.getPayload();
                receivedSignal.countDown();
            });

            receivedSignal.await(5, TimeUnit.SECONDS);


            client.publish(PATH_CONTROL, setMqttMessage("{ left: 5.0 }"));
            client.publish(PATH_TELEMETRY, setMqttMessage(String.format("T:%04.2f",10.0)));
           // call();

        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testDeleteDeviceInfo() {
        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getDeviceRegistryStable(),
                        PATH_REGISTRATION + "ASSYSTEM_TENANT4" + "/" + "assystem2"))
                .delete()
                .headers(getBaseRequestHeaders())
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }



	public void testDeleteTenantInfo() {
		// GIVEN

        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getDeviceRegistryStable(),
                        PATH_CREDENTIALS + "ASSYSTEM_TENANT4"))
                .delete()
                .build();
		// WHEN

		ResponseEntity<String> responseEntity = executeApiCall(request);

		// THEN
		assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());

	}

    public String createDeviceCredentials() throws NoSuchAlgorithmException {
        String deviceId = "assystem1";
        String tenantId = "ASSYSTEM_TENANT4";
        String password = "whySoSecret";

        String username = deviceId + "@" + tenantId;

        return getHashCodeFromString(password);
    }

    public static String getHashCodeFromString(String str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(str.getBytes());
        byte byteData[] = md.digest();

        String hashCodeBuffer = (Base64.getEncoder().encodeToString(byteData));
        return hashCodeBuffer;
    }

    @Test
    public void postDeviceCredentials() throws JSONException, NoSuchAlgorithmException {
        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getDeviceRegistryStable(), PATH_CREDENTIALS + "ASSYSTEM_TENANT4"))
                .post()
                .headers(getBaseRequestHeaders())
                .body(new JSONObject()
                        .put("device-id", "assystem1")
                        .put("type", "hashed-password")
                        .put("auth-id", "assystem1")
                        .put("secrets", new JSONArray()
                                .put(new JSONObject()
                                        .put("pwd-hash", createDeviceCredentials())
                                        .put("hash-function", "sha-512")
                                        .put("not-after", "2020-03-31T00:00:00+01:00")
                                )
                        )
                )
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    public void getDeviceCredentials() {
        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getDeviceRegistryStable(),
                        PATH_CREDENTIALS + "ASSYSTEM_TENANT4" + "/" + "assystem1" + "/hashed-password"))
                .get()
                .headers(getBaseRequestHeaders())
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void deleteDeviceCredentials() throws JSONException {
        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getDeviceRegistryStable(),
                        PATH_CREDENTIALS + "ASSYSTEM_TENANT4" + "/" + "assystem1" + "/hashed-password"))
                .delete()
                .headers(getBaseRequestHeaders())
                .body(new JSONObject()
                        .put("type", "hashed-password")
                        .put("auth-id", "assystem1")
                )
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }

}
