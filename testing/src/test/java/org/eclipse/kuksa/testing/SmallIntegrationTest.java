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

import org.apache.logging.log4j.Level;
import org.eclipse.kuksa.testing.client.Request;
import org.eclipse.kuksa.testing.config.*;
import org.eclipse.kuksa.testing.model.Credentials;
import org.eclipse.kuksa.testing.model.TestData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;

import java.io.*;
import java.util.Date;
import java.util.Random;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import feign.Response;
import org.springframework.web.multipart.MultipartFile;

@Ignore
@EnableAutoConfiguration
@ContextConfiguration(classes = {GlobalConfiguration.class, HawkBitConfiguration.class, AppStoreConfiguration.class, HawkbitMultiPartFileFeignClient.class})
public class SmallIntegrationTest extends AbstractTestCase {
    private static final String JSON_PROPERTY_ID = "id";

    private static String securityToken;

    private static Credentials hawkbitCredentials;

    private static String hawkbitAddress;

    private static Credentials appstoreCredentials;

    private static String appstoreAddress;

    private static String controllerId;

    private static String appname;

    private static String deviceId;

    private static Long userId;

    private static JSONObject user;

    private static String userName;

    private static Long softwareModuleId;

    private static Long categoryId;

    private static JSONObject category;

    private static Long appId;

    private static JSONObject app;

    private static JSONObject JSON_PROPERTY_APP_CATEGORY_NAME_VALUE = new JSONObject();

    @Autowired
    private GlobalConfiguration globalConfig;

    @Autowired
    private HawkBitConfiguration hawkbitConfig;

    @Autowired
    private AppStoreConfiguration appstoreConfig;

    @Autowired
    private HawkbitMultiPartFileFeignClient hawkbitMultiPartFileFeignClient;

    @Override
    protected String getTestFile() {
        return "SmallIntegration-TestSuite.yaml";
    }

    @Test
    @Order(1)
    public void integrationSetup() throws Exception {
        TestData testData = testCase.getTestData(0);

        appname = testData.getAppname() + new Random().nextInt(1000);

        deviceId = testData.getDeviceId();

        System.out.println("SETUP START");

        hawkbitAddress = hawkbitConfig.getAddress();
        hawkbitCredentials = new Credentials(hawkbitConfig.getUsername(), hawkbitConfig.getPassword());

        appstoreAddress = appstoreConfig.getAddress();
        appstoreCredentials = new Credentials(appstoreConfig.getUsername(), appstoreConfig.getPassword());

        System.out.println("CREATING USER");

        user = new JSONObject(createUser(testData.getUsername(), testData.getPassword(), testData.getUserType()));
        userId = user.getLong(JSON_PROPERTY_ID);
        userName = user.getString("username");

        System.out.println("CREATING CATEGORY");

        category = new JSONObject(createCategory(testData.getCategory()));
        categoryId = category.getLong(JSON_PROPERTY_ID);

        System.out.println("CREATING APP");

        app = new JSONObject(createApp(categoryId, testData.getVersion(), testData.getOwner(), testData.getUsername(), appname));
        appId = app.getLong(JSON_PROPERTY_ID) ;

        System.out.println("userId " + userId + " categoryId " + categoryId + " appId " + appId);
        addAppToUser();

        System.out.println("SETTING UP HAWKBIT");

        if(!testCheckForTarget()) {

            String responseBody = createTarget();
            JSONArray jsonArray = new JSONArray(responseBody);
            String element = jsonArray.getString(0);
            JSONObject body = new JSONObject(element);

            controllerId = body.getString("controllerId");
            securityToken = body.getString("securityToken");

            System.out.println(body);
        }

        updateTarget(testData.getAuthToken());
        getSoftwareModule();
    }

    @AfterClass
    public static void integrationCleanup() {
        System.out.println("CLEANUP START");

        if (appId != null) {
            removeApp(appId);
            removeUser(userId);
            removeCategory(categoryId);
            removeTargetForCleanup(controllerId);
        }
    }

    public boolean testCheckForTarget() throws JSONException {
        HttpHeaders headers = new HttpHeaders(getBaseRequestHeaders());
        headers.setBasicAuth("admin", "admin");

        Request request = new Request.Builder()
                .url(buildUrl(hawkbitConfig.getAddress(),  "/rest/v1/targets"))
                .get()
                .headers(headers)
                .build();

        ResponseEntity<String> responseEntity = executeApiCall(request);

        System.out.println(responseEntity.getBody());

        JSONArray array = new JSONObject(responseEntity.getBody()).getJSONArray("content");
        for (int i = 0; i < array.length(); i++) {
            JSONObject row = array.getJSONObject(i);
            System.out.println(row + "\n\n");
            if(row.getString("controllerId").equals(deviceId)) {

                controllerId = row.getString("controllerId");
                securityToken = row.getString("securityToken");

                return true;
            }
        }
        return false;
    }

    public void testGetTargetInfo() {
        Request request = new Request.Builder()
                .url(buildUrl(hawkbitConfig.getAddress(),  "/rest/controller/v1/" + controllerId))
                .get()
                .headers(getBaseRequestHeaders())
                .addHeader("Authorization", "TargetToken " + securityToken)
                .build();

        ResponseEntity<String> responseEntity = executeApiCall(request);

        String body = responseEntity.getBody();
        assertNotNull(body);
    }

    public void getSoftwareModule() throws Exception {
        System.out.println("GETTING OUR NEW SOFTWAREMODULE");

        Request request = new Request.Builder()
                .url(buildUrl(hawkbitConfig.getAddress(),  "/rest/v1/softwaremodules"))
                .get()
                .credentials(hawkbitCredentials)
                .headers(getBaseRequestHeaders())
                .build();

        ResponseEntity<String> responseEntity = executeApiCall(request);

        JSONArray array = new JSONObject(responseEntity.getBody()).getJSONArray("content");
        for (int i = 0; i < array.length(); i++) {
            JSONObject row = array.getJSONObject(i);
            System.out.println(row + "\n\n");
            if(row.getString("name").equals(appname)) {
                softwareModuleId = row.getLong("id");
            }
        }
        uploadArtifactToHawkbit();
    }

    public void uploadArtifactToHawkbit() {
        ClassLoader classLoader = new HonoConfiguration().getClass().getClassLoader();
        File file = new File(classLoader.getResource("dummy").getFile());

        MultipartFile imageFile = new MultipartFile() {

            @Override
            public String getName() {
                return file.getName();
            }

            @Override
            public String getOriginalFilename() {
                return file.getName();
            }

            @Override
            public String getContentType() {
                return MediaType.MULTIPART_FORM_DATA_VALUE;
            }

            @Override
            public boolean isEmpty() {
                return readContentIntoByteArray(file).length == 0;
            }

            @Override
            public long getSize() {
                return readContentIntoByteArray(file).length;
            }

            @Override
            public byte[] getBytes() {
                return readContentIntoByteArray(file);
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(readContentIntoByteArray(file));
            }

            @Override
            public void transferTo(File dest) throws IllegalStateException {

            }
        };

        Response response = hawkbitMultiPartFileFeignClient.uploadFile(softwareModuleId.toString(), imageFile);

        System.out.println(response.body());

        testPurchaseApp();
    }

    private static String createTarget() throws JSONException {
        // build request
        String controllerId = deviceId;
        String name = deviceId;
        String description = userName;

        JSONArray requestBody = new JSONArray()
                .put(
                        new JSONObject()
                                .put("controllerId", controllerId)
                                .put("name", name)
                                .put("description", description)
                );

        Request request = new Request.Builder()
                .url(buildUrl(hawkbitAddress, "/rest/v1/targets"))
                .post()
                .headers(getBaseRequestHeaders())
                .body(requestBody)
                .credentials(hawkbitCredentials)
                .build();

        // execute request
        ResponseEntity<String> responseEntity = executeApiCall(request);

        if (responseEntity.getStatusCodeValue() != HttpStatus.CREATED.value()) {
            fail("Failed to create target.");
        }

        return responseEntity.getBody();
    }

    public void updateTarget(String authToken) throws JSONException {

        Request request = new Request.Builder()
                .url(buildUrl(hawkbitAddress, "rest/v1/targets/" + controllerId))
                .put()
                .credentials(hawkbitCredentials)
                .body(new JSONObject()
                        .put("securityToken", authToken)
                        .put( "requestAttributes", true)
                        .put("controllerId", controllerId)
                        .put("name", controllerId)
                        .put("description", userName))
                .headers(getBaseRequestHeaders())
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        System.out.println(responseEntity.getBody());
    }

    public void testPurchaseApp() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(appstoreCredentials.getUsername(), appstoreCredentials.getPassword());

        Request request = getBaseRequestBuilder()
                .post()
                .url(buildUrl(appstoreAddress, "/api/1.0/app/" + appId + "/purchase/" + userId))
                .headers(headers)
                .build();

        // WHEN
        ResponseEntity<String> response = executeApiCall(request);

        System.out.println(response.getBody());
    }

    @Test
    @Order(3)
    public void installationTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(appstoreCredentials.getUsername(), appstoreCredentials.getPassword());

        System.out.println("Installing app to " + controllerId);

        Request request = new Request.Builder()
                .url(buildUrl(appstoreAddress, "/api/1.0/app/" + appId + "/install/" + userId))
                .post()
                .body(controllerId)
                .headers(headers)
                .build();

        // WHEN
        ResponseEntity<String> responseEntity = executeApiCall(request);

        System.out.println(responseEntity.getBody());

        System.out.println("Waiting for success response from device");
        Thread.sleep(20000);
    }

    private static Request.Builder getBaseRequestBuilder() {
        return new Request.Builder()
                .headers(getBaseRequestHeaders())
                .credentials(hawkbitCredentials);
    }

    private static String createUser(String username, String password, String userType) throws JSONException {
        Request request = getBaseRequestBuilder()
                .post()
                .url(buildUrl(appstoreAddress, "/api/1.0/user/"))
                .body(new JSONObject()
                        .put("adminuser", false)
                        .put("username", username)
                        .put("password", password)
                        .put("userType", userType)
                )
                .build();

        ResponseEntity<String> response = executeApiCall(request);
        LOGGER.info("JUST CREATED " +  response.getBody());

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to create app store user."));
        }

        return response.getBody();
    }

    private static String createCategory(String category) throws JSONException {
        Request request = getBaseRequestBuilder()
                .post()
                .url(buildUrl(appstoreAddress, "/api/1.0/appcategory/"))
                .body(new JSONObject()
                        .put("name", category)
                )
                .build();

        ResponseEntity<String> response = executeApiCall(request);
        LOGGER.log(Level.INFO, "JUST CREATED " +  response.getBody());

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to create app store category."));
        }

        return response.getBody();
    }

    private static String createApp(Long categoryId, String version, String owner, String description, String appname) throws JSONException {
        try {
            JSON_PROPERTY_APP_CATEGORY_NAME_VALUE = new JSONObject().put("id", categoryId ).put("name", category.getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Request request = getBaseRequestBuilder()
                .post()
                .url(buildUrl(appstoreAddress, "/api/1.0/app/"))
                .body(new JSONObject()
                        .put("name", appname)
                        .put("version", version)
                        .put("description", description)
                        .put("hawkbitname", appname)
                        .put("owner", owner )
                        .put("appcategory", JSON_PROPERTY_APP_CATEGORY_NAME_VALUE )
                        .put("downloadcount", 0 )
                        .put("publishdate", new Date().getTime())
                )
                .build();

        ResponseEntity<String> response = executeApiCall(request);
        System.out.println("JUST CREATED " +  response.getBody());

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to create app store app."));
        }
        return response.getBody();
    }

    private static void addAppToUser() throws Exception {

        Request requestApp = getBaseRequestBuilder()
                .put()
                .url(buildUrl(appstoreAddress, "/api/1.0/app/" + appId))
                .body(new JSONObject(app.toString())
                        .put("installedusers", new JSONArray().put(user))
                )
                .build();

        ResponseEntity<String> response = executeApiCall(requestApp);

        if (!response.getStatusCode().is2xxSuccessful()) {
            fail("Failed to update app.");
        }

    }

    private static void removeCategory(Long categoryId) {
        Request request = getBaseRequestBuilder()
                .delete()
                .url(buildUrl(appstoreAddress, "/api/1.0/appcategory/" + categoryId))
                .build();

        ResponseEntity<String> response = executeApiCall(request);

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to remove app store category."));
        }
    }

    private static void removeApp(Long appId) {
        Request request = getBaseRequestBuilder()
                .delete()
                .url(buildUrl(appstoreAddress, "/api/1.0/app/" + appId))
                .build();

        ResponseEntity<String> response = executeApiCall(request);

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to remove app store app."));
        }
    }

    private static void removeUser(Long userId) {
        Request request = getBaseRequestBuilder()
                .delete()
                .url(buildUrl(appstoreAddress, "/api/1.0/user/" + userId))
                .build();

        ResponseEntity<String> response = executeApiCall(request);

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error(new Exception("Failed to remove app store user."));
        }
    }

    private static void removeTargetForCleanup(String controllerId) {
        // build request
        Request request = new Request.Builder()
                .url(buildUrl(hawkbitAddress, "/rest/v1/targets/" + controllerId))
                .delete()
                .headers(getBaseRequestHeaders())
                .credentials(hawkbitCredentials)
                .build();

        // execute request
        ResponseEntity<String> responseEntity = executeApiCall(request);

        if (responseEntity.getStatusCodeValue() != HttpStatus.OK.value()) {
            fail("Failed to remove target.");
        }
    }


    private static byte[] readContentIntoByteArray(File file)
    {
        FileInputStream fileInputStream = null;
        byte[] bFile = new byte[(int) file.length()];
        try
        {
            //convert file into array of bytes
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();
            for (int i = 0; i < bFile.length; i++)
            {
                System.out.print((char) bFile[i]);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return bFile;
    }

}


