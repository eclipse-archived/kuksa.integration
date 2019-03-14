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

import org.eclipse.kuksa.testing.client.Request;
import org.eclipse.kuksa.testing.config.GlobalConfiguration;
import org.eclipse.kuksa.testing.config.HonoConfiguration;
import org.eclipse.kuksa.testing.model.Credentials;
import org.eclipse.kuksa.testing.model.ResponseResult;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;
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

    private static final String PATH_MQTT_TELEMETRY = "rover/1/telemetry";

    private static final String PATH_HTTP_TELEMETRY = "/telemetry";

    private static final String PATH_CREDENTIALS = "/credentials/";

    @Autowired
    private static HonoConfiguration config;

    private static String tenant_id = "ASSYSTEM_TENANT4";
    private static String device_id = "assystem2";

    @BeforeClass
    public static void setup() throws JSONException, NoSuchAlgorithmException {
        createTenant(tenant_id);
        createDevice(tenant_id, device_id);
        createCredentials();
    }

    @AfterClass
    public static void clean() {
        removeTenant(tenant_id);
        removeDevice(tenant_id, device_id);
        removeCredentials();
    }

    @Override
    protected String getTestFile() {
        return "Hono-TestSuite.yaml";
    }

    public static void createTenant(String tenant) throws JSONException {
        // GIVEN

        System.out.println("print: " + config.getDeviceRegistryStable());

        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getDeviceRegistryStable(), PATH_TENANT))
                .post()
                .headers(getBaseRequestHeaders())
                .body(new JSONObject()
                        .put("tenant-id", tenant))
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        System.out.println("createTenant" + responseEntity);

    }

    public static void createDevice(String tenant, String device) throws JSONException {
        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getDeviceRegistryStable(), PATH_REGISTRATION + tenant))
                .post()
                .headers(getBaseRequestHeaders())
                .body(new JSONObject()
                        .put("device-id", device))
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        System.out.println("createDevice" + responseEntity);

    }

    public static void removeTenant(String tenant) {
        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getDeviceRegistryStable(),
                        PATH_TENANT + tenant))
                .delete()
                .build();
        // WHEN

        ResponseEntity<String> responseEntity = executeApiCall(request);

        System.out.println("removeTenant" + responseEntity);

    }

    public static void removeDevice(String tenant, String device) {
        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getDeviceRegistryStable(),
                        PATH_REGISTRATION + tenant + "/" + device))
                .delete()
                .headers(getBaseRequestHeaders())
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        System.out.println("removeDevice" + responseEntity);

    }

    @Test
    public void testGetTenantInfo() {
        // GIVEN
        String tenantId = "ASSYSTEM_TENANT4";

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
    }

    @Test
    public void testGetDeviceInfo() {
        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getDeviceRegistryStable(), PATH_REGISTRATION + tenant_id +"/" + device_id))
                .get()
                .headers(getBaseRequestHeaders())
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        JSONObject body = getBodyAsJson(responseEntity);
        assertNotNull(body);

        assertEquals(device_id, getJsonValue(body, "device-id"));
    }

    @Test
    public void testPostTenantInfo() throws JSONException {
        String tenant = "ASSYSTEM_TENANT";
        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getDeviceRegistryStable(), PATH_TENANT))
                .post()
                .headers(getBaseRequestHeaders())
                .body(new JSONObject()
                        .put("tenant-id", tenant))
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

    //    JSONObject body = getBodyAsJson(responseEntity);
    //    assertNotNull(body);

        removeTenant(tenant);
    }

    @Test
    public void testPostDeviceInfo() throws JSONException {
        String tenant = "DEFAULT_TENANT";
        String device = "assystem1";

        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getDeviceRegistryStable(), PATH_REGISTRATION + tenant))
                .post()
                .headers(getBaseRequestHeaders())
                .body(new JSONObject()
                        .put("device-id", device))
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        removeDevice(tenant, device);
 //       JSONObject body = getBodyAsJson(responseEntity);
 //       assertNotNull(body);
    }

    @Test
    public void testPostTelemetryData() throws JSONException {
        // GIVEN
        String username = "assystem2@ASSYSTEM_TENANT4";
        String password = "whySoSecret";

        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getAdapterHttpVertxStable(), PATH_HTTP_TELEMETRY))
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

    @Test
    public void testPostEventData() throws JSONException {
        // GIVEN
        String username = "assystem2@ASSYSTEM_TENANT4";
        String password = "whySoSecret";

        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getAdapterHttpVertxStable(), PATH_HTTP_TELEMETRY))
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
            options.setUserName("assystem2@ASSYSTEM_TENANT4");
            options.setPassword("whySoSecret".toCharArray());
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
            client.subscribe(PATH_MQTT_TELEMETRY, (topic, msg) -> {
                byte[] payload = msg.getPayload();
                receivedSignal.countDown();
            });

            receivedSignal.await(5, TimeUnit.SECONDS);


            client.publish(PATH_CONTROL, setMqttMessage("{ left: 5.0 }"));
            client.publish(PATH_MQTT_TELEMETRY, setMqttMessage(String.format("T:%04.2f",10.0)));
           // call();

        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    @Ignore
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

    @Ignore
    @Test
	public void testDeleteTenantInfo() {
		// GIVEN

        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getDeviceRegistryStable(),
                        PATH_TENANT + "ASSYSTEM_TENANT4"))
                .delete()
                .build();
		// WHEN

		ResponseEntity<String> responseEntity = executeApiCall(request);

		// THEN
		assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());

	}

    public static String createDeviceCredentials() throws NoSuchAlgorithmException {
        String deviceId = "assystem2";
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

    @Ignore
    @Test
    public void postDeviceCredentials() throws JSONException, NoSuchAlgorithmException {
        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getDeviceRegistryStable(), PATH_CREDENTIALS + "ASSYSTEM_TENANT4"))
                .post()
                .headers(getBaseRequestHeaders())
                .body(new JSONObject()
                        .put("device-id", "assystem2")
                        .put("type", "hashed-password")
                        .put("auth-id", "assystem2")
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

    public static void createCredentials() throws JSONException, NoSuchAlgorithmException {
        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getDeviceRegistryStable(), PATH_CREDENTIALS + "ASSYSTEM_TENANT4"))
                .post()
                .headers(getBaseRequestHeaders())
                .body(new JSONObject()
                        .put("device-id", "assystem2")
                        .put("type", "hashed-password")
                        .put("auth-id", "assystem2")
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
    }

    @Test
    public void getDeviceCredentials() {
        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getDeviceRegistryStable(),
                        PATH_CREDENTIALS + "ASSYSTEM_TENANT4" + "/" + "assystem2" + "/hashed-password"))
                .get()
                .headers(getBaseRequestHeaders())
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }


    public static void removeCredentials() {
        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getDeviceRegistryStable(),
                        PATH_CREDENTIALS + "ASSYSTEM_TENANT4" + "/" + "assystem2" + "/hashed-password"))
                .delete()
                .headers(getBaseRequestHeaders())
             /*   .body(new JSONObject()
                        .put("type", "hashed-password")
                        .put("auth-id", "assystem2")
                )*/
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);
        System.out.println("removeCreds" + responseEntity);

    }


    @Ignore
    @Test
    public void deleteDeviceCredentials() throws JSONException {
        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(PROTOCOL_HTTP, config.getDeviceRegistryStable(),
                        PATH_CREDENTIALS + "ASSYSTEM_TENANT4" + "/" + "assystem2" + "/hashed-password"))
                .delete()
                .headers(getBaseRequestHeaders())
                .body(new JSONObject()
                        .put("type", "hashed-password")
                        .put("auth-id", "assystem2")
                )
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }

}
