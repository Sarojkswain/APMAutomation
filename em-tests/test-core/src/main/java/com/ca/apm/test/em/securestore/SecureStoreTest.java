/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.test.em.securestore;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.test.ClwRunner;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.wily.introscope.appmap.common.crypto.ISecureStore;
import com.wily.introscope.appmap.common.crypto.SecureStoreClient;
import com.wily.isengard.KIsengardConstants;
import com.wily.isengard.api.IIsengardClient;
import com.wily.isengard.api.IsengardClient;
import com.wily.isengard.api.ServerInstanceLocator;
import com.wily.isengard.api.TransportConfiguration;
import com.wily.isengard.messageprimitives.ConnectionException;
import com.wily.util.feedback.ApplicationFeedback;
import com.wily.util.feedback.IModuleFeedbackChannel;
import com.wily.util.io.NexusLocator;
import com.wily.util.thread.DefaultThreadFactory;

@Tas(testBeds = @TestBed(name = SecureStoreTestBed.class, executeOn = SecureStoreTestBed.EM_MACHINE), owner = "svazd01", size = SizeType.SMALL)
@Test(groups = "secureStore")
public class SecureStoreTest extends TasTestNgTest {


    private String hostname;

    private int port;

    private ISecureStore secureStore = null;

    private EnvironmentPropertyContext envProp;

    private static final char[] PLAINTEXT =
        "some plain text to encrypt quick brown fox jumps over the lazy dog".toCharArray();

    private static final char[] PLAINTEXT_2 = "another quick borwn fox jumsp over another lazy dog"
        .toCharArray();

    private static final char[] PLAINTEXT_4 = "why not here comes fourth".toCharArray();

    private static final char[] PLAINTEXT_5 = "42 is the ansver. But what is the question?"
        .toCharArray();


    private static String USER_1;

    private static String USER_2;

    @BeforeTest
    public void loadEnvProperties() throws Exception {
        envProp = new EnvironmentPropertyContextFactory().createFromSystemProperty();
        hostname = getHostName();
        // TODO get port from TAS properties
        port = 5001;

        connect();

        // generate original usernames in case test is launched more times on EM with same DB
        USER_1 = "user_1_" + System.currentTimeMillis();
        USER_2 = "user_2_" + System.currentTimeMillis();
    }

    @Test
    public void testEncryptAndUpdate() throws Exception {
        assertNotNull(secureStore);


        String alias = secureStore.storeEncrypted(PLAINTEXT);
        char[] decrypt = secureStore.fetchDecrypted(alias);

        assertTrue(Arrays.equals(decrypt, PLAINTEXT));

        decrypt = secureStore.fetchDecrypted(alias);
        assertTrue(Arrays.equals(decrypt, PLAINTEXT));

        assertNull(secureStore.fetchDecrypted(alias, USER_1));



        assertTrue(secureStore.storeEncrypted(PLAINTEXT_2, alias, true));

        decrypt = secureStore.fetchDecrypted(alias);

        assertTrue(Arrays.equals(decrypt, PLAINTEXT_2));
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "(.*)userId cannot be null(.*)")
    public void testNullUsername() throws ConnectionException {
        secureStore.storeEncrypted(PLAINTEXT_2, "null", true, null);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "(.*)alias cannot be null(.*)")
    public void testNullAlias() throws ConnectionException {
        secureStore.storeEncrypted(PLAINTEXT_2, null, true, USER_1);
    }

    @Test
    public void testEncryptAndUpdateWithUsername() throws Exception {

        USER_1 = "user_1_" + System.currentTimeMillis();
        USER_2 = "user_2_" + System.currentTimeMillis();

        String alias = secureStore.storeEncrypted(PLAINTEXT, USER_1);
        assertNotNull(alias);


        assertNull(secureStore.fetchDecrypted(alias));
        assertNull(secureStore.fetchDecrypted(alias, USER_2));

        char[] decrypt = secureStore.fetchDecrypted(alias, USER_1);
        assertTrue(Arrays.equals(decrypt, PLAINTEXT));

        // for user alias does nto exist so true it is
        assertTrue(secureStore.storeEncrypted(PLAINTEXT_2, alias, false));
        assertTrue(secureStore.storeEncrypted(PLAINTEXT_4, alias, false, USER_2));


        assertTrue(Arrays.equals(secureStore.fetchDecrypted(alias), PLAINTEXT_2));
        assertTrue(Arrays.equals(secureStore.fetchDecrypted(alias, USER_2), PLAINTEXT_4));
        assertTrue(Arrays.equals(secureStore.fetchDecrypted(alias, USER_1), PLAINTEXT));


        // ///////////// TEST FALSE UPSERT ////////////////////////////
        assertFalse(secureStore.storeEncrypted(PLAINTEXT_5, alias, false));
        assertFalse(secureStore.storeEncrypted(PLAINTEXT_5, alias, false, USER_2));
        assertFalse(secureStore.storeEncrypted(PLAINTEXT_5, alias, false, USER_1));

        // nothing should change in false upsert
        assertTrue(Arrays.equals(secureStore.fetchDecrypted(alias), PLAINTEXT_2));
        assertTrue(Arrays.equals(secureStore.fetchDecrypted(alias, USER_2), PLAINTEXT_4));
        assertTrue(Arrays.equals(secureStore.fetchDecrypted(alias, USER_1), PLAINTEXT));
        // ///////////// TEST FALSE UPSERT ////////////////////////////


        // /////////// TEST TRUE UPSERT ////////////////////////
        assertTrue(secureStore.storeEncrypted(PLAINTEXT_5, alias, true));
        assertTrue(secureStore.storeEncrypted(PLAINTEXT_5, alias, true, USER_2));
        assertTrue(secureStore.storeEncrypted(PLAINTEXT_5, alias, true, USER_1));



        // ALL SHOULD should change in false upsert
        assertTrue(Arrays.equals(secureStore.fetchDecrypted(alias), PLAINTEXT_5));
        assertTrue(Arrays.equals(secureStore.fetchDecrypted(alias, USER_2), PLAINTEXT_5));
        assertTrue(Arrays.equals(secureStore.fetchDecrypted(alias, USER_1), PLAINTEXT_5));



    }


    @Test
    public void testUpdateNonExistent() throws ConnectionException, Exception {
        String alias = UUID.randomUUID().toString();

        assertTrue(secureStore.storeEncrypted(PLAINTEXT_2, alias, true));

        char[] decrypted = secureStore.fetchDecrypted(alias);

        assertTrue(Arrays.equals(decrypted, PLAINTEXT_2));
    }

    @Test
    public void testFetchNonExistent() {
        char[] retVlue = secureStore.fetchDecrypted("nonsens_noexistent_key");
        assertNull(retVlue);
    }

    @Test
    public void testCreateEixstent() throws ConnectionException, Exception {

        String alias = secureStore.storeEncrypted(PLAINTEXT);


        assertFalse(secureStore.storeEncrypted(PLAINTEXT_2, alias, false));
        assertTrue(secureStore.storeEncrypted(PLAINTEXT_2, alias, false, USER_1));

        char[] decrypted = secureStore.fetchDecrypted(alias);

        assertTrue(Arrays.equals(decrypted, PLAINTEXT));

        decrypted = secureStore.fetchDecrypted(alias, USER_1);

        assertTrue(Arrays.equals(decrypted, PLAINTEXT_2));


    }

    /**
     * 
     * 
     * @throws ConnectionException
     * @throws Exception
     */
    @Test
    public void testMultipleAliases() throws ConnectionException, Exception {
        String alias = secureStore.storeEncrypted(PLAINTEXT);
        secureStore.storeEncrypted(PLAINTEXT_2, alias, true, USER_1);

        assertTrue(Arrays.equals(PLAINTEXT, secureStore.fetchDecrypted(alias)));
        assertTrue(Arrays.equals(PLAINTEXT_2, secureStore.fetchDecrypted(alias, USER_1)));


        assertTrue(secureStore.storeEncrypted(PLAINTEXT_4, alias, true));

        assertTrue(Arrays.equals(PLAINTEXT_4, secureStore.fetchDecrypted(alias)));
        assertTrue(Arrays.equals(PLAINTEXT_2, secureStore.fetchDecrypted(alias, USER_1)));


        assertFalse(secureStore.storeEncrypted(PLAINTEXT, alias, false));

        assertTrue(Arrays.equals(PLAINTEXT_4, secureStore.fetchDecrypted(alias)));
        assertTrue(Arrays.equals(PLAINTEXT_2, secureStore.fetchDecrypted(alias, USER_1)));


        assertTrue(secureStore.storeEncrypted(PLAINTEXT_5, alias, true, USER_1));

        assertTrue(Arrays.equals(PLAINTEXT_4, secureStore.fetchDecrypted(alias)));
        assertTrue(Arrays.equals(PLAINTEXT_5, secureStore.fetchDecrypted(alias, USER_1)));


        assertFalse(secureStore.storeEncrypted(PLAINTEXT, alias, false, USER_1));

        assertTrue(Arrays.equals(PLAINTEXT_4, secureStore.fetchDecrypted(alias)));
        assertTrue(Arrays.equals(PLAINTEXT_5, secureStore.fetchDecrypted(alias, USER_1)));



    }

    @Test
    public void testExistUpsertTrue() throws ConnectionException {
        String alias = secureStore.storeEncrypted(PLAINTEXT, USER_1);
        assertTrue(secureStore.storeEncrypted(PLAINTEXT_2, alias, true, USER_1));
        assertTrue(Arrays.equals(PLAINTEXT_2, secureStore.fetchDecrypted(alias, USER_1)));
    }

    @Test
    public void testExistUpsertFalse() throws ConnectionException {
        String alias = secureStore.storeEncrypted(PLAINTEXT, USER_1);
        assertFalse(secureStore.storeEncrypted(PLAINTEXT_2, alias, false, USER_1));
        assertTrue(Arrays.equals(PLAINTEXT, secureStore.fetchDecrypted(alias, USER_1)));
    }

    @Test
    public void testNoexistingUpsertTrue() throws ConnectionException {
        String alias = "password";
        assertTrue(secureStore.storeEncrypted(PLAINTEXT, alias, true, USER_1));
        assertTrue(Arrays.equals(PLAINTEXT, secureStore.fetchDecrypted(alias, USER_1)));
    }

    @Test
    public void testNoexistenUpsertFalse() throws ConnectionException {
        String alias = "password";
        assertTrue(secureStore.storeEncrypted(PLAINTEXT, alias, false, USER_1));
        assertTrue(Arrays.equals(PLAINTEXT, secureStore.fetchDecrypted(alias, USER_1)));
    }

    @Test
    public void testHashAndMatch() throws ConnectionException, Exception {
        String hashed1 = secureStore.hashPassword(PLAINTEXT);
        String hashed2 = secureStore.hashPassword(PLAINTEXT);
        assertTrue(secureStore.matchPassword(PLAINTEXT, hashed1));
        assertTrue(secureStore.matchPassword(PLAINTEXT, hashed2));
    }

    @Test
    public void testHashEmpty() throws ConnectionException, Exception {
        String hashed1 = secureStore.hashPassword("".toCharArray());
        assertTrue(secureStore.matchPassword("".toCharArray(), hashed1));
    }

    // @Test
    // public void testChangeAlgorythmAndDecryptExisting() throws ConnectionException, Exception {
    //
    // String alias_aes128 = secureStore.storeEncrypted(PLAINTEXT);
    //
    // stopSetEncAlgStart("DES56");
    //
    // String alias_des56 = secureStore.storeEncrypted(PLAINTEXT);
    //
    // assertTrue(Arrays.equals(secureStore.fetchDecrypted(alias_aes128), PLAINTEXT));
    //
    // stopSetEncAlgStart("DES168");
    //
    // String alias_des168 = secureStore.storeEncrypted(PLAINTEXT);
    //
    // assertTrue(Arrays.equals(secureStore.fetchDecrypted(alias_aes128), PLAINTEXT));
    // assertTrue(Arrays.equals(secureStore.fetchDecrypted(alias_des56), PLAINTEXT));
    //
    // stopSetEncAlgStart("AES128");
    //
    // assertTrue(Arrays.equals(secureStore.fetchDecrypted(alias_aes128), PLAINTEXT));
    // assertTrue(Arrays.equals(secureStore.fetchDecrypted(alias_des56), PLAINTEXT));
    // assertTrue(Arrays.equals(secureStore.fetchDecrypted(alias_des168), PLAINTEXT));
    //
    // }
    private void stopSetEncAlgStart(String alg) throws Exception {
        ClwRunner clwRuner = utilities.createClwUtils(SecureStoreTestBed.ROLE).getClwRunner();
        utilities.createEmUtils().stopLocalEm(clwRuner, SecureStoreTestBed.ROLE);
        utilities.createPortUtils().waitTillLocalPortIsAvailableInSec(port, 60 * 1000);

        // refresh log files we need multiple restart
        String logPath =
            (String) envProp.getRolePropertiesById(SecureStoreTestBed.ROLE).get(
                DeployEMFlowContext.ENV_EM_LOG_FILE);
        File log = new File(logPath);
        log.delete();
        log.createNewFile();

        setEncryptionAlgorythm(alg);



        utilities.createEmUtils().startLocalEm(SecureStoreTestBed.ROLE);

        connect();

    }


    private void setEncryptionAlgorythm(String algorythm) throws IOException {
        String configpath =
            (String) envProp.getRolePropertiesById(SecureStoreTestBed.ROLE).get("emConfigFile");
        File configFile = new File(configpath);

        if (configFile.exists()) {
            Properties iemp = new Properties();
            iemp.load(new FileInputStream(configFile));
            iemp.put("introscope.secure.store.encryption.algorithm", algorythm);
            iemp.store(new FileOutputStream(configFile), "new d");
        }
    }


    private String getHostName() throws UnknownHostException {
        String hostname = envProp.getRolePropertiesById("role_em").getProperty("em_hostname");
        if (hostname == null || hostname.isEmpty()) {
            hostname = "localhost";
        }

        return InetAddress.getByName(hostname).getCanonicalHostName();
    }
    
    



    private void connect() throws Exception {
        ApplicationFeedback fFeedback = new ApplicationFeedback("SSTEST");
        NexusLocator locator = new NexusLocator(hostname, port);
        IsengardClient isengardClient = connectToEM(locator, fFeedback);

        secureStore = new SecureStoreClient(isengardClient.getMainPO());
    }

    private IsengardClient connectToEM(NexusLocator locator, IModuleFeedbackChannel feedback)
        throws IOException {
        ServerInstanceLocator serverLocator =
            new ServerInstanceLocator(locator.getNexusHostName(), locator.getNexusPort(), feedback);

        IsengardClient client =
            new IsengardClient(feedback, KIsengardConstants.kWorkstationGroup,
                KIsengardConstants.kWorkstationPassword,
                TransportConfiguration.getDefaultClientConfiguration(), serverLocator,
                new IIsengardClient() {
                    public void lostConnectionToIsengardServer() {
                        // thanks, but we
                        // don't really care
                    }

                    public void connectedToIsengardServer() {
                        // do nothing
                    }
                }, getClass().getClassLoader(), new DefaultThreadFactory(false));
        client.setCacheRegistry(false);
        client.connect();

        return client;
    }


}
