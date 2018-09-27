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

/**
 * 
 */
package com.ca.apm.test.em.securestore;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

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
import com.wily.util.feedback.ApplicationFeedback;
import com.wily.util.feedback.IModuleFeedbackChannel;
import com.wily.util.io.NexusLocator;
import com.wily.util.thread.DefaultThreadFactory;

/**
 * @author svazd01
 *
 */
public class SecureStoreLoadTest extends TasTestNgTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecureStoreLoadTest.class);

    private static List<MethodStatistic> statistics = Collections
        .synchronizedList(new ArrayList<MethodStatistic>());


    private static Set<String> setOfGeneratedAliases = new HashSet<String>();


    private static final String[] userIds = {"user1", "user2", "user4", "user5", "user6", "user7",
            "user8", "user9"};

    private static final String[] aliases = {"alias0", "alias1", "alias2", "alias3", "alias4",
            "alias5", "alias6", "alias7", "alias8", "alias9", "alias10", "alias11", "alias12",
            "alias13", "alias14", "alias15"};

    private static final Map<String, String> hashedString = new HashMap<String, String>();

    private int numberOfThreadsPerEm;

    private int numberOfRequestsPerThread;

    @Tas(testBeds = @TestBed(name = SecureStoreLoadTestBed.class, executeOn = SecureStoreLoadTestBed.COL1_MACHINE), size = SizeType.MEDIUM, owner = "svazd01")
    @Test(groups = {"secure_store_load_test", "secure_store_loa_test10_500"})
    public void test10_500() throws Exception {
        testLoad(10, 500);
    }

    @Tas(testBeds = @TestBed(name = SecureStoreLoadTestBed.class, executeOn = SecureStoreLoadTestBed.COL1_MACHINE), size = SizeType.MEDIUM, owner = "svazd01")
    @Test(groups = {"secure_store_load_test", "secure_store_loa_test50_500"})
    public void test50_500() throws Exception {
        testLoad(50, 500);
    }

    @Tas(testBeds = @TestBed(name = SecureStoreLoadTestBed.class, executeOn = SecureStoreLoadTestBed.COL1_MACHINE), size = SizeType.MEDIUM, owner = "svazd01")
    @Test(groups = {"secure_store_load_test", "secure_store_loa_test100_500"})
    public void test100_500() throws Exception {
        testLoad(100, 500);
    }

    @Tas(testBeds = @TestBed(name = SecureStoreLoadTestBed.class, executeOn = SecureStoreLoadTestBed.COL1_MACHINE), size = SizeType.MEDIUM, owner = "svazd01")
    @Test(groups = {"secure_store_load_test", "secure_store_loa_test200_500"})
    public void test200_500() throws Exception {
        testLoad(200, 500);
    }

    @Tas(testBeds = @TestBed(name = SecureStoreLoadTestBed.class, executeOn = SecureStoreLoadTestBed.COL1_MACHINE), size = SizeType.MEDIUM, owner = "svazd01")
    @Test(groups = {"secure_store_load_test", "secure_store_loa_test300_500"})
    public void test300_500() throws Exception {
        testLoad(300, 500);
    }

    @Tas(testBeds = @TestBed(name = SecureStoreLoadTestBed.class, executeOn = SecureStoreLoadTestBed.COL1_MACHINE), size = SizeType.MEDIUM, owner = "svazd01")
    @Test(groups = {"secure_store_load_test", "secure_store_loa_test400_500"})
    public void test400_500() throws Exception {
        testLoad(400, 500);
    }

    @Tas(testBeds = @TestBed(name = SecureStoreLoadTestBed.class, executeOn = SecureStoreLoadTestBed.COL1_MACHINE), size = SizeType.MEDIUM, owner = "svazd01")
    @Test(groups = {"secure_store_load_test", "secure_store_loa_test500_500"})
    public void test500_500() throws Exception {
        testLoad(500, 500);
    }



    private String getMomHostname() throws UnknownHostException {

        String host =
            envProperties.getRolePropertiesById(SecureStoreLoadTestBed.MOM_ROLE).getProperty(
                "em_hostname");

        return InetAddress.getByName(host).getCanonicalHostName();
    }

    private String getCol1Hostname() throws UnknownHostException {

        String host =
            envProperties.getRolePropertiesById(SecureStoreLoadTestBed.COL1_ROLE).getProperty(
                "em_hostname");

        return InetAddress.getByName(host).getCanonicalHostName();
    }

    private String getCol2Hostname() throws UnknownHostException {

        String host =
            envProperties.getRolePropertiesById(SecureStoreLoadTestBed.COL2_ROLE).getProperty(
                "em_hostname");

        return InetAddress.getByName(host).getCanonicalHostName();
    }

    private String getCol3Hostname() throws UnknownHostException {

        String host =
            envProperties.getRolePropertiesById(SecureStoreLoadTestBed.COL3_ROLE).getProperty(
                "em_hostname");

        return InetAddress.getByName(host).getCanonicalHostName();
    }

    public void testLoad(int numberOfThreads, int numberOfRequests) throws Exception {

        numberOfThreadsPerEm = numberOfThreads;
        numberOfRequestsPerThread = numberOfRequests;

        ExecutorService es = Executors.newCachedThreadPool();

        IsengardClient momClient = connect(getMomHostname(), 5001);
        IsengardClient col1Client = connect(getCol1Hostname(), 5001);
        IsengardClient col2Client = connect(getCol2Hostname(), 5001);
        IsengardClient col3Client = connect(getCol3Hostname(), 5001);


        for (int i = 0; i < numberOfThreadsPerEm; i++) {
            es.execute(new TestsThread(getMomHostname(), 5001, numberOfRequestsPerThread, momClient));
            es.execute(new TestsThread(getCol1Hostname(), 5001, numberOfRequestsPerThread,
                col1Client));
            es.execute(new TestsThread(getCol2Hostname(), 5001, numberOfRequestsPerThread,
                col2Client));
            es.execute(new TestsThread(getCol3Hostname(), 5001, numberOfRequestsPerThread,
                col3Client));
        }

        es.shutdown();
        while (!es.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // boolean finshed = es.awaitTermination(50, TimeUnit.MINUTES);

        List<Long> timeMom = new LinkedList<Long>();

        List<Long> allTime = new LinkedList<Long>();

        List<Long> timeCol1 = new LinkedList<Long>();

        List<Long> timeCol2 = new LinkedList<Long>();

        List<Long> timeCol3 = new LinkedList<Long>();



        for (MethodStatistic s : statistics) {
            if (s.timeFinished != 0 && s.exception == null) {
                long time = s.timeFinished - s.timeLaunched;
                if ("tas-cz-na5".equals(s.host)) {
                    timeMom.add(time);
                } else if ("tas-cz-n87".equals(s.host)) {
                    timeCol1.add(time);
                } else if ("tas-cz-n8a".equals(s.host)) {
                    timeCol2.add(time);
                } else if ("tas-cz-n99".equals(s.host)) {
                    timeCol3.add(time);
                }

                allTime.add(time);
            }
        }

        Collections.sort(timeMom);
        Collections.sort(allTime);
        Collections.sort(timeCol1);
        Collections.sort(timeCol2);
        Collections.sort(timeCol3);


        long[] mom = ArrayUtils.toPrimitive(timeMom.toArray(new Long[timeMom.size()]));
        long[] all = ArrayUtils.toPrimitive(allTime.toArray(new Long[allTime.size()]));
        long[] col1 = ArrayUtils.toPrimitive(timeCol1.toArray(new Long[timeCol1.size()]));
        long[] col2 = ArrayUtils.toPrimitive(timeCol2.toArray(new Long[timeCol2.size()]));
        long[] col3 = ArrayUtils.toPrimitive(timeCol3.toArray(new Long[timeCol3.size()]));

       
                    

        
        
        

            LOGGER.info(String.format("for 1 MOM and 3 Collectors each requested by %d clients each of them sent %d requests. Times below were measured",numberOfThreadsPerEm, numberOfRequestsPerThread));
            
            if(all.length != 0) LOGGER.info(String.format("ALL : avg:%-10.2f median:%-10.2f modus:%-10d max:%-10d min:%-10d ",mean(all), median(all), mode(all), max(all),min(all)));
            if(mom.length != 0) LOGGER.info(String.format("MOM : avg:%-10.2f median:%-10.2f modus:%-10d max:%-10d min:%-10d ",mean(mom), median(mom), mode(mom), max(mom),min(mom)));
            
            if(col1.length != 0) LOGGER.info(String.format("COL1: avg:%-10.2f median:%-10.2f modus:%-10d max:%-10d min:%-10d ",mean(col1), median(col1), mode(col1), max(col1),min(col1)));
            if(col2.length != 0) LOGGER.info(String.format("COL2: avg:%-10.2f median:%-10.2f modus:%-10d max:%-10d min:%-10d ",mean(col2), median(col2), mode(col2), max(col2),min(col2)));
            if(col3.length != 0) LOGGER.info(String.format("COL3: avg:%-10.2f median:%-10.2f modus:%-10d max:%-10d min:%-10d ",mean(col3), median(col3), mode(col3), max(col3),min(col3)));
            
        
        

      

    }



    public static double mean(long[] m) {
        long sum = 0;
        for (int i = 0; i < m.length; i++) {
            sum += m[i];
        }
        return sum / m.length;
    }


    // the array double[] m MUST BE SORTED
    public static double median(long[] m) {
        int middle = m.length / 2;
        if (m.length % 2 == 1) {
            return m[middle];
        } else {
            return (m[middle - 1] + m[middle]) / 2.0;
        }
    }


    public static long mode(long a[]) {
        long maxValue = 0;
        long maxCount = 0;

        for (int i = 0; i < a.length; ++i) {
            int count = 0;
            for (int j = 0; j < a.length; ++j) {
                if (a[j] == a[i]) ++count;
            }
            if (count > maxCount) {
                maxCount = count;
                maxValue = a[i];
            }
        }

        return maxValue;
    }


    public static long min(long[] arr) {
        long min = Long.MAX_VALUE;
        for (int i = 0; i < arr.length; ++i) {
            if (arr[i] < min) {
                min = arr[i];
            }
        }
        return min;
    }


    public static long max(long[] arr) {
        long max = 0;
        for (int i = 0; i < arr.length; ++i) {
            if (arr[i] > max) {
                max = arr[i];
            }
        }
        return max;
    }


    private enum Method {
        storeEncrypted, storeEncrypted_2, storeEncrypted_upsert, storeEncrypted_upsert_2, fetchDecrypted, fetchDecrypted_2, hashPassword, matchPassword;

        private static List<Method> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
        private static final int SIZE = VALUES.size();
        private static final Random RANDOM = new Random();

        public static Method random() {
            return VALUES.get(RANDOM.nextInt(SIZE));
        }

    }



    private class MethodStatistic implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        String host;

        Method method;
        long timeLaunched;
        // 0 if failedR
        long timeFinished;

        Throwable exception;
        String message;

        Map<String, Object> params = new HashMap<String, Object>();
        Object result;



        public MethodStatistic(String host, Method method) {
            super();
            this.host = host;
            this.method = method;
        }



        @Override
        public String toString() {
            return "MethodStatistic [host=" + host + ", method=" + method + ", timeLaunched="
                + timeLaunched + ", timeFinished=" + timeFinished + ", exception=" + exception
                + ", params=" + params + ", result=" + result + "]";
        }



    }


    private class TestsThread implements Runnable {

        private String host;

        private int port;

        IsengardClient client;

        private ISecureStore secureStore;


        private int numberOfLoops;

        private SecureRandom random = new SecureRandom();

        public TestsThread(String host, int port, int numberOfLoops) {

            this.host = host;
            this.port = port;
            this.numberOfLoops = numberOfLoops;

            try {
                client = connect(host, port);

                secureStore = new SecureStoreClient(client.getMainPO());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        public TestsThread(String host, int port, int numberOfLoops, IsengardClient client) {

            this.host = host;
            this.port = port;
            this.numberOfLoops = numberOfLoops;

            try {
                this.client = client;

                secureStore = new SecureStoreClient(this.client.getMainPO());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        public String randomUserName() {
            return userIds[random.nextInt(userIds.length - 1)];
        }

        public String randomAliasName() {
            return aliases[random.nextInt(aliases.length - 1)];
        }

        public String getRandomGenAlias() {

            if (setOfGeneratedAliases.isEmpty() || random.nextDouble() > 0.1) {
                return "noexistent_alias_12345";
            }
            return (String) setOfGeneratedAliases.toArray()[random.nextInt(setOfGeneratedAliases
                .toArray().length - 1)];
        }


        public String randomHashedString() {
            if (hashedString.isEmpty()) {
                return "noexistent_alias_12345";
            }
            return (String) hashedString.values().toArray()[random.nextInt(hashedString.values()
                .toArray().length - 1)];
        }

        public char[] randomPlainText() {
            return new BigInteger(80, random).toString(32).toCharArray();
        }

        public Boolean randomBoolean() {
            return (Boolean) random.nextBoolean();
        }


        @Override
        public void run() {
            for (int i = 0; i < numberOfLoops; i++) {
                call(i);
            }
        }

        public void call(int i) {
            MethodStatistic method = new MethodStatistic(host, Method.random());
            try {
                switch (method.method) {
                    case storeEncrypted:
                        storeEncrypted(method);
                        break;
                    case storeEncrypted_2:
                        storeEncrypted_2(method);
                        break;
                    case fetchDecrypted:
                        fetchDecrypted(method);
                        break;
                    case fetchDecrypted_2:
                        fetchDecrypted_2(method);
                        break;
                    case storeEncrypted_upsert:
                        storeEncrypted_upsert(method);
                        break;
                    case storeEncrypted_upsert_2:
                        storeEncrypted_upsert_2(method);
                        break;
                    case hashPassword:
                        hashPassword(method);
                        break;
                    case matchPassword:
                        matchPassword(method);
                        break;

                }
            } catch (Throwable t) {
                method.exception = t;
                method.message = t.getMessage();
            }

            // LOGGER.trace(String.format("Request %d. to %s: %s", i, host, method.toString()));

            statistics.add(method);

        }



        public void storeEncrypted(MethodStatistic method) {
            char[] plaintext = randomPlainText();

            method.timeLaunched = System.currentTimeMillis();
            String result = secureStore.storeEncrypted(plaintext);
            method.timeFinished = System.currentTimeMillis();

            method.params.put("text", new String(plaintext));
            method.result = result;
        }

        public void storeEncrypted_2(MethodStatistic method) {

            char[] text = randomPlainText();
            String userId = randomUserName();

            method.timeLaunched = System.currentTimeMillis();
            String result = secureStore.storeEncrypted(text, userId);
            method.timeFinished = System.currentTimeMillis();

            method.params.put("text", new String(text));
            method.params.put("userId", userId);
            method.result = result;

        }

        public void fetchDecrypted(MethodStatistic method) {

            String alias = getRandomGenAlias();

            method.timeLaunched = System.currentTimeMillis();
            char[] result = secureStore.fetchDecrypted(alias);
            method.timeFinished = System.currentTimeMillis();

            method.params.put("alias", alias);
            method.result = new String(result);
        }

        public void fetchDecrypted_2(MethodStatistic method) {
            String alias = randomAliasName();
            String userId = randomUserName();

            method.timeLaunched = System.currentTimeMillis();
            char[] result = secureStore.fetchDecrypted(alias, userId);
            method.timeFinished = System.currentTimeMillis();

            method.params.put("alias", alias);
            method.params.put("userId", userId);
            method.result = new String(result);
        }

        public void storeEncrypted_upsert(MethodStatistic method) {
            char[] text = randomPlainText();
            String alias = randomAliasName();
            Boolean upsert = randomBoolean();

            method.timeLaunched = System.currentTimeMillis();
            boolean result = secureStore.storeEncrypted(text, alias, upsert);
            method.timeFinished = System.currentTimeMillis();

            method.params.put("text", text);
            method.params.put("alias", alias);
            method.params.put("upsert", upsert);
            method.result = result;

        }

        public void storeEncrypted_upsert_2(MethodStatistic method) {
            char[] text = randomPlainText();
            String alias = randomAliasName();
            String userId = randomUserName();
            Boolean upsert = randomBoolean();

            method.timeLaunched = System.currentTimeMillis();
            boolean result = secureStore.storeEncrypted(text, alias, upsert, userId);
            method.timeFinished = System.currentTimeMillis();

            method.params.put("text", text);
            method.params.put("alias", alias);
            method.params.put("userId", userId);
            method.params.put("upsert", upsert);
            method.result = result;
        }

        public void hashPassword(MethodStatistic method) throws Exception {
            char[] random_string = randomAliasName().toCharArray();

            method.timeLaunched = System.currentTimeMillis();
            String result = secureStore.hashPassword(random_string);
            method.timeFinished = System.currentTimeMillis();

            hashedString.put(new String(random_string), result);

            method.params.put("password", new String(random_string));
            method.result = result;
        }

        public void matchPassword(MethodStatistic method) throws Exception {
            char[] password = randomAliasName().toCharArray();
            String ciphertext = randomHashedString();

            method.timeLaunched = System.currentTimeMillis();
            boolean result = secureStore.matchPassword(password, ciphertext);
            method.timeFinished = System.currentTimeMillis();

            method.params.put("password", new String(password));
            method.params.put("ciphertext", ciphertext);
            method.result = result;
        }



    }



    private IsengardClient connect(String host, int port) throws Exception {
        ApplicationFeedback fFeedback = new ApplicationFeedback("SSTEST");
        NexusLocator locator = new NexusLocator(host, port);
        return connectToEM(locator, fFeedback);

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
