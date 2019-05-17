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
import org.eclipse.kuksa.testing.model.TestData;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.internal.security.SSLSocketFactoryFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@ContextConfiguration(classes = {GlobalConfiguration.class, HonoConfiguration.class})
public class HonoApiTest extends AbstractTestCase {

    private static final String PATH_TENANT = "/tenant/";

    private static final String PATH_REGISTRATION = "/registration/";

    private static final String PATH_CONTROL = "control/+/+/req/#"; // "/rover/1/RoverDriving/control";

    private static final String PATH_MQTT_TELEMETRY = "/rover/1/telemetry";

    private static final String PATH_HTTP_TELEMETRY = "/telemetry";

    private static final String PATH_CREDENTIALS = "/credentials/";

    @Autowired
    private static HonoConfiguration config;

    private static String tenant_id = "ASSYSTEM_TENANT4";
    private static String device_id = "assystem2";

    private static IMqttClient client;

    @BeforeClass
    public static void setup() throws JSONException, NoSuchAlgorithmException, IOException {
        createTenant(tenant_id);
        createDevice(tenant_id, device_id);
        createCredentials();

        // publish
        String publisherId = UUID.randomUUID().toString();
        try {
            client = new MqttClient(config.getAdapterMqttVertxStable(), publisherId);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setUserName("sensor1@DEFAULT_TENANT");
            options.setPassword("hono-secret".toCharArray());
            options.setConnectionTimeout(10);
            Properties sslProperties = new Properties();
            ClassLoader classLoader = new HonoConfiguration().getClass().getClassLoader();
            sslProperties.put(SSLSocketFactoryFactory.TRUSTSTORE, new File(classLoader.getResource("trustStore.jks").getFile()));
            sslProperties.put(SSLSocketFactoryFactory.TRUSTSTOREPWD, "honotrust");
            sslProperties.put(SSLSocketFactoryFactory.TRUSTSTORETYPE, "JKS");
            sslProperties.put(SSLSocketFactoryFactory.CLIENTAUTH, false);
            options.setSSLProperties(sslProperties);
            // actually connect the client
            client.connect(options);

            // receive
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    LOGGER.info("MQTT Error - Connection lost", throwable);
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) {
                    System.out.println("MQTT Message arrived successfully: " + mqttMessage);
                    assertNotNull(mqttMessage.getPayload());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    try {
                        System.out.println("MQTT Message delivery completed " + iMqttDeliveryToken.getMessage());
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    assertEquals(true, iMqttDeliveryToken.isComplete());
                }
            });

            CountDownLatch receivedSignal = new CountDownLatch(10);
//
//            client.subscribe(PATH_MQTT_TELEMETRY, (topic, msg) -> {
//                receivedSignal.countDown();
//                System.out.println("Something arrived: at " + PATH_MQTT_TELEMETRY + msg.toString());
//            });

            client.subscribe(PATH_CONTROL, (topic, msg) -> {
                receivedSignal.countDown();
                System.out.println("Something arrived at : " + PATH_CONTROL + msg.toString());

            });

            receivedSignal.await(10, TimeUnit.SECONDS);

        } catch (MqttException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("MQTT Interrupt");
        }

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

        Request request = new Request.Builder()
                .url(buildUrl(config.getDeviceRegistryStable(), PATH_TENANT))
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
                .url(buildUrl(config.getDeviceRegistryStable(), PATH_REGISTRATION + tenant))
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
                .url(buildUrl(config.getDeviceRegistryStable(),
                        PATH_TENANT + tenant))
                .delete()
                .build();
        // WHEN

        ResponseEntity<String> responseEntity = executeApiCall(request);

        System.out.println("removeTenant" + responseEntity);
    }

    public static void removeDevice(String tenant, String device) {
        Request request = new Request.Builder()
                .url(buildUrl(config.getDeviceRegistryStable(),
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
        TestData testData = testCase.getTestData(0);;

        Request request = new Request.Builder()
                .url(buildUrl(config.getDeviceRegistryStable(), PATH_TENANT + testData.getTenantId()))
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

        assertEquals(testData.getTenantId(), getJsonValue(body, "tenant-id"));
    }

    @Test
    public void testGetDeviceInfo() {

        TestData testData = testCase.getTestData(0);;

        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(config.getDeviceRegistryStable(), PATH_REGISTRATION + testData.getTenantId() +"/" + testData.getDeviceId()))
                .get()
                .headers(getBaseRequestHeaders())
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);
        ResponseResult result = testCase.getResult(0);

        // THEN
        assertEquals(result.getStatusCode(), responseEntity.getStatusCodeValue());

        JSONObject body = getBodyAsJson(responseEntity);
        assertNotNull(body);

        assertEquals(testData.getDeviceId(), getJsonValue(body, "device-id"));
    }

    @Test
    public void testPostTenantInfo() throws JSONException {
        TestData testData = testCase.getTestData(0);
        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(config.getDeviceRegistryStable(), PATH_TENANT))
                .post()
                .headers(getBaseRequestHeaders())
                .body(new JSONObject()
                        .put("tenant-id", testData.getTenantId()))
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);
        ResponseResult result = testCase.getResult(0);

        // THEN
        assertEquals(result.getStatusCode(), responseEntity.getStatusCodeValue());

        removeTenant(testData.getTenantId());
    }

    @Test
    public void testPostDeviceInfo() throws JSONException {
        TestData testData = testCase.getTestData(0);

        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(config.getDeviceRegistryStable(), PATH_REGISTRATION + testData.getTenantId()))
                .post()
                .headers(getBaseRequestHeaders())
                .body(new JSONObject()
                        .put("device-id", testData.getDeviceId()))
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);
        ResponseResult result = testCase.getResult(0);

        // THEN
        assertEquals(result.getStatusCode(), responseEntity.getStatusCodeValue());

        removeDevice(testData.getTenantId(), testData.getDeviceId());
    }

    @Test
    public void testPostTelemetryData() throws JSONException {
        TestData testData = testCase.getTestData(0);

        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(config.getAdapterHttpVertxStable(), PATH_HTTP_TELEMETRY))
                .post()
                .headers(getBaseRequestHeaders())
                .body(testData.getBody())
                .credentials(new Credentials(testData.getUsername(), testData.getPassword()))
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);
        ResponseResult result = testCase.getResult(0);

        // THEN
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());
    }

    @Test
    public void testPostEventData() {
        TestData testData = testCase.getTestData(0);

        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(config.getAdapterHttpVertxStable(), PATH_HTTP_TELEMETRY))
                .post()
                .headers(getBaseRequestHeaders())
                .body(testData.getBody())
                .credentials(new Credentials(testData.getUsername(), testData.getPassword()))
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);
        ResponseResult result = testCase.getResult(0);

        // THEN
        assertEquals(result.getStatusCode(), response.getStatusCodeValue());
    }

    @Test
    public void testPublishControlData() {
        try {
//            client.publish(PATH_CONTROL , setMqttMessage("{ left: 10.0 }"));
            client.publish(PATH_MQTT_TELEMETRY , setMqttMessage(String.format("temperature:%04.2f",11.0)));

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Ignore
    @Test
    public void testDeleteDeviceInfo() {
        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(config.getDeviceRegistryStable(),
                        PATH_REGISTRATION + "ASSYSTEM_TENANT4" + "/" + "assystem2"))
                .delete()
                .headers(getBaseRequestHeaders())
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCodeValue());
    }

    @Ignore
    @Test
	public void testDeleteTenantInfo() {
		// GIVEN

        Request request = new Request.Builder()
                .url(buildUrl(config.getDeviceRegistryStable(),
                        PATH_TENANT + "ASSYSTEM_TENANT4"))
                .delete()
                .build();
		// WHEN

		ResponseEntity<String> responseEntity = executeApiCall(request);

		// THEN
		assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCodeValue());

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
                .url(buildUrl(config.getDeviceRegistryStable(), PATH_CREDENTIALS + "ASSYSTEM_TENANT4"))
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
                .url(buildUrl(config.getDeviceRegistryStable(), PATH_CREDENTIALS + "ASSYSTEM_TENANT4"))
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
                .url(buildUrl(config.getDeviceRegistryStable(),
                        PATH_CREDENTIALS + "ASSYSTEM_TENANT4" + "/" + "assystem2" + "/hashed-password"))
                .get()
                .headers(getBaseRequestHeaders())
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);
        ResponseResult result = testCase.getResult(0);

        // THEN
        assertEquals(result.getStatusCode(), responseEntity.getStatusCodeValue());
    }


    public static void removeCredentials() {
        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(config.getDeviceRegistryStable(),
                        PATH_CREDENTIALS + "ASSYSTEM_TENANT4" + "/" + "assystem2" + "/hashed-password"))
                .delete()
                .headers(getBaseRequestHeaders())
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
                .url(buildUrl(config.getDeviceRegistryStable(),
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
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCodeValue());
    }

}
