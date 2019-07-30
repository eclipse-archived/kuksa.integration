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
 * @author: alezor
 **********************************************************************/

package org.eclipse.kuksa.testing;

import org.eclipse.kuksa.testing.client.Request;
import org.eclipse.kuksa.testing.client.Commander;
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
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@ContextConfiguration(classes = {GlobalConfiguration.class, HonoConfiguration.class})
public class HonoApiTest extends AbstractTestCase {

    private static final String PATH_TENANT = "/tenant/";

    private static final String PATH_REGISTRATION = "/registration/";

    private static final String PATH_CONTROL = "control/+/+/req/#";

    private static final String PATH_HTTP_TELEMETRY = "/telemetry";

    private static final String PATH_CREDENTIALS = "/credentials/";

    private static MqttMessage message;

    private static String tenant_id = "EXPLEO_TENANT4";
    private static String device_id = "expleo2";

    private static Commander commander;
    private static IMqttClient client;

    @BeforeClass
    public static void setup() throws JSONException, NoSuchAlgorithmException {
        createTenant(tenant_id);
        createDevice(tenant_id, device_id);
        createCredentials();

        commander = new Commander();
        commander.start();
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

    @Test
    public void testGetTenantInfo() {
        TestData testData = testCase.getTestData(0);

        System.out.println("Testdata " + testData);

        Request request = new Request.Builder()
                .url(buildUrl(HonoConfiguration.getDeviceRegistryStable(), PATH_TENANT + testData.getTenantId()))
                .get()
                .headers(getBaseRequestHeaders())
                .build();

        ResponseEntity<String> responseEntity = executeApiCall(request);

        ResponseResult result = testCase.getResult(0);

        assertEquals(result.getStatusCode(), responseEntity.getStatusCodeValue());

        JSONObject body = getBodyAsJson(responseEntity);
        assertNotNull(body);

        assertEquals(result.getBody(), body.toString());

        assertEquals(testData.getTenantId(), getJsonValue(body, "tenant-id"));
    }

    @Test
    public void testGetDeviceCredentials() {
        TestData testData = testCase.getTestData(0);

        Request request = new Request.Builder()
                .url(buildUrl(HonoConfiguration.getDeviceRegistryStable(),
                        PATH_CREDENTIALS + testData.getTenantId() + "/" + testData.getDeviceId() + "/" + testData.getRoute()))
                .get()
                .headers(getBaseRequestHeaders())
                .build();

        ResponseEntity<String> responseEntity = executeApiCall(request);
        ResponseResult result = testCase.getResult(0);

        assertEquals(result.getStatusCode(), responseEntity.getStatusCodeValue());
    }

    @Test
    @Order(1)
    public void testGetDeviceInfo() {
        TestData testData = testCase.getTestData(0);

        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(HonoConfiguration.getDeviceRegistryStable(), PATH_REGISTRATION + testData.getTenantId() +"/" + testData.getDeviceId()))
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
    @Order(2)
    public void testPostTenantInfo() throws JSONException {
        TestData testData = testCase.getTestData(0);

        Request request = new Request.Builder()
                .url(buildUrl(HonoConfiguration.getDeviceRegistryStable(), PATH_TENANT))
                .post()
                .headers(getBaseRequestHeaders())
                .body(new JSONObject()
                        .put("tenant-id", testData.getTenantId()))
                .build();

        ResponseEntity<String> responseEntity = executeApiCall(request);
        ResponseResult result = testCase.getResult(0);

        assertEquals(result.getStatusCode(), responseEntity.getStatusCodeValue());

        removeTenant(testData.getTenantId());
    }

    @Test
    @Order(3)
    public void testPostDeviceInfo() throws JSONException {
        TestData testData = testCase.getTestData(0);

        Request request = new Request.Builder()
                .url(buildUrl(HonoConfiguration.getDeviceRegistryStable(), PATH_REGISTRATION + testData.getTenantId()))
                .post()
                .headers(getBaseRequestHeaders())
                .body(new JSONObject()
                        .put("device-id", testData.getDeviceId()))
                .build();

        ResponseEntity<String> responseEntity = executeApiCall(request);
        ResponseResult result = testCase.getResult(0);

        assertEquals(result.getStatusCode(), responseEntity.getStatusCodeValue());

        removeDevice(testData.getTenantId(), testData.getDeviceId());
    }

    @Test
    @Order(4)
    public void testPublishTelemetryData() {
        TestData testData = testCase.getTestData(0);

        Request request = new Request.Builder()
                .url(buildUrl(HonoConfiguration.getAdapterHttpVertxStable(), PATH_HTTP_TELEMETRY))
                .post()
                .headers(getBaseRequestHeaders())
                .body(testData.getBody())
                .credentials(new Credentials(testData.getUsername(), testData.getPassword()))
                .build();

        ResponseEntity<String> response = executeApiCall(request);
        ResponseResult result = testCase.getResult(0);

        assertEquals(result.getStatusCode(), response.getStatusCodeValue());
    }

    @Test
    @Order(5)
    public void testPublishEventData() {
        TestData testData = testCase.getTestData(0);

        Request request = new Request.Builder()
                .url(buildUrl(HonoConfiguration.getAdapterHttpVertxStable(), PATH_HTTP_TELEMETRY))
                .post()
                .headers(getBaseRequestHeaders())
                .body(testData.getBody())
                .credentials(new Credentials(testData.getUsername(), testData.getPassword()))
                .build();

        ResponseEntity<String> response = executeApiCall(request);
        ResponseResult result = testCase.getResult(0);

        assertEquals(result.getStatusCode(), response.getStatusCodeValue());
    }

    @Test
    @Order(6)
    public void testPublishControlData() {
        TestData testData = testCase.getTestData(0);

        String publisherId = UUID.randomUUID().toString();

        try {
            client = new MqttClient(HonoConfiguration.getAdapterMqttVertxStable(), publisherId);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setUserName(testData.getUsername());
            options.setPassword(testData.getPassword().toCharArray());
            options.setConnectionTimeout(1);

            Properties sslProperties = new Properties();
            ClassLoader classLoader = new HonoConfiguration().getClass().getClassLoader();
            sslProperties.put(SSLSocketFactoryFactory.KEYSTORE,  new File(classLoader.getResource("DSTRootX3.pem").getFile()));
            sslProperties.put(SSLSocketFactoryFactory.KEYSTORETYPE, "PEM");
            sslProperties.put(SSLSocketFactoryFactory.CLIENTAUTH, false);
            options.setSSLProperties(sslProperties);
            // actually connect the client
            // receive
            client.setManualAcks(false);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    LOGGER.info("MQTT Error - Connection lost", throwable);
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws MqttException {
                    System.out.println("MQTT Message " +  mqttMessage.getId()+ " arrived successfully: " + mqttMessage);
                    message = mqttMessage;
                    client.messageArrivedComplete(mqttMessage.getId(), 1);
                    assertNotNull(mqttMessage.getPayload());
                    client.close();
                    commander.close();
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken)  {
                    try {
                        System.out.println("MQTT Message delivery completed " + iMqttDeliveryToken.getMessage());
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    System.out.println(iMqttDeliveryToken.getResponse());
                    assertTrue(iMqttDeliveryToken.isComplete());
                }
            });

            client.connectWithResult(options);

            CountDownLatch receivedSignal = new CountDownLatch(6);

            client.subscribeWithResponse(PATH_CONTROL, 1, (topic, msg) -> {
                receivedSignal.countDown();
                System.out.println("Something arrived at : " + PATH_CONTROL + msg.toString());

            });

            receivedSignal.await(6, TimeUnit.SECONDS);

        } catch (MqttException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("MQTT Interrupt");
        }

        assertNotNull(message);
    }

    public static String createDeviceCredentials() throws NoSuchAlgorithmException {
        String password = "whySoSecret";

        return getHashCodeFromString(password);
    }

    public static String getHashCodeFromString(String str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(str.getBytes());
        byte byteData[] = md.digest();

        return Base64.getEncoder().encodeToString(byteData);
    }

    public static void createCredentials() throws JSONException, NoSuchAlgorithmException {
        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(HonoConfiguration.getDeviceRegistryStable(), PATH_CREDENTIALS + "EXPLEO_TENANT4"))
                .post()
                .headers(getBaseRequestHeaders())
                .body(new JSONObject()
                        .put("device-id", "expleo2")
                        .put("type", "hashed-password")
                        .put("auth-id", "expleo2")
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
        executeApiCall(request);
    }

    public static void createTenant(String tenant) throws JSONException {
        // GIVEN

        Request request = new Request.Builder()
                .url(buildUrl(HonoConfiguration.getDeviceRegistryStable(), PATH_TENANT))
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
                .url(buildUrl(HonoConfiguration.getDeviceRegistryStable(), PATH_REGISTRATION + tenant))
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
                .url(buildUrl(HonoConfiguration.getDeviceRegistryStable(),
                        PATH_TENANT + tenant))
                .delete()
                .build();
        // WHEN

        ResponseEntity<String> responseEntity = executeApiCall(request);

        System.out.println("removeTenant" + responseEntity);
    }

    public static void removeDevice(String tenant, String device) {
        Request request = new Request.Builder()
                .url(buildUrl(HonoConfiguration.getDeviceRegistryStable(),
                        PATH_REGISTRATION + tenant + "/" + device))
                .delete()
                .headers(getBaseRequestHeaders())
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        System.out.println("removeDevice" + responseEntity);
    }

    public static void removeCredentials() {
        // GIVEN
        Request request = new Request.Builder()
                .url(buildUrl(HonoConfiguration.getDeviceRegistryStable(),
                        PATH_CREDENTIALS + "EXPLEO_TENANT4" + "/" + "expleo2" + "/hashed-password"))
                .delete()
                .headers(getBaseRequestHeaders())
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);
        System.out.println("removeCreds" + responseEntity);
        assertEquals(responseEntity.getStatusCodeValue(), 204);
    }

}
