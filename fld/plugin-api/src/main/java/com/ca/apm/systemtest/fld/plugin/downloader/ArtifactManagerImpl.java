package com.ca.apm.systemtest.fld.plugin.downloader;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author KEYJA01
 */
public class ArtifactManagerImpl implements ArtifactManager {
    private static Logger log = LoggerFactory.getLogger(ArtifactManagerImpl.class);

    //TODO - DM - remove this guy at all from here

    @Override
    @Deprecated
    public ArtifactFetchResult fetchArtifact(String artifactSpecification, RepositoryType type,
        File destinationDirectory, Map<String, Object> parameters)
        throws ArtifactManagerException {
        if (log.isDebugEnabled()) {
            log.debug("artifact specification: {}", artifactSpecification);
            log.debug("repository type: {}", type);
            log.debug("destination directory: {}", destinationDirectory);
            log.debug("parameters: {}", parameters);
        }

        if (type == null) {
            final String msg = "Missing repository type";
            log.error(msg);
            throw new ArtifactManagerException(msg);
        }

        if (destinationDirectory == null) {
            final String msg = "Missing destination directory";
            log.error(msg);
            throw new ArtifactManagerException(msg);
        }
        throw new UnsupportedOperationException();

//        switch (type) {
//            case ARTIFACTORY:
//                //TODO - DM artifactory.fetch ?
////                fetchResult = fetchMaven(artifactSpecification, destinationDirectory, parameters);
//                break;
//
//            case ARTIFACTORYLITE:
////                fetchResult = fetch(artifactSpecification, destinationDirectory, parameters);
//                break;
//
//            case HTTP:
//                fetchResult = http.fetch(artifactSpecification, destinationDirectory, parameters, true);
//                break;
//
//            case TRUSS:
//                fetchResult = truss.fetch(artifactSpecification, destinationDirectory, parameters, true);
//                break;
//
//            case FILESYSTEM:
//                fetchResult = null;
//                break;
//
//            default:
//                fetchResult = null;
//                break;
//        }
//
//        if (log.isDebugEnabled()) {
//            log.debug("fetch result: {}", fetchResult);
//        }

//        return fetchResult;
    }


    // Copied out of maven-embedder/src/main/java/org/apache/maven/cli/MavenCli.java.


//    static DefaultRepositorySystemSession newRepositorySystemSession(
//        RepositorySystem system) {
//        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
//
//        LocalRepository localRepo = new LocalRepository("target/local-repo");
//        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
//
//        session.setTransferListener(new ConsoleTransferListener());
//        session.setRepositoryListener(new ConsoleRepositoryListener());
//
//        // uncomment to generate dirty trees
//        // session.setDependencyGraphTransformer( null );
//
//        return session;
//    }
//
//
//    static RepositorySystem newRepositorySystem() {
//         /*
//         * Aether's components implement org.eclipse.aether.spi.locator.Service to ease manual
//         * wiring and using the
//         * prepopulated DefaultServiceLocator, we only need to register the repository connector
//         * factories.
//         */
//        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
//        locator.addService( RepositoryConnectorFactory.class, FileRepositoryConnectorFactory.class );
//        locator.addService( RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class );
//        locator.setServices( WagonProvider.class, new WagonProvider() {
//            public Wagon lookup(String roleHint) throws Exception {
//                if (Arrays.asList("http", "https").contains(roleHint)) {
//                    return new Wagon();
//                }
//                return null;
//            }
//
//            public void release(Wagon wagon) {
//
//            }
//        });
//        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
//        //locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);
//        //locator.setServices(WagonProvider.class, new ManualWagonProvider());
//
//        return locator.getService(RepositorySystem.class);
//    }




//    public void setFileDownloadCacheUrl(String fileDownloadCacheUrl) {
//        this.fileDownloadCacheUrl = fileDownloadCacheUrl;
//    }

//    public static void main(String[] args) throws Exception {
//        ArtifactManagerImpl am = new ArtifactManagerImpl();
//
//        // Example downloading workstation installer from artifactory
//        Map<String, Object> parameters = new HashMap<>(10);
//        parameters.put(KEY_REPO_BASE, "http://oerth-scx.ca.com:8081/artifactory/repo/");
//        am.fetchMaven("com.ca.apm.delivery:workstation-installer-windows:9.7.0.19:exe:windows",
//            new File("C:\\Foo\\tmp\\"), parameters);
//
//        // Example spec for downloading TIM from Truss.  The plugin should maintain specs for the
//        // products it is supposed to download.
//        String spec = "${" + KEY_REPO_BASE + "}/${" + KEY_CODE_NAME + "}/build-${"
//            + KEY_BUILD_NUMBER + "}(${" + KEY_BUILD_ID + "})/${" + KEY_PRODUCT + "}-${"
//            + KEY_BUILD_ID + "}.${" + KEY_BUILD_SUFFIX + "}.Linux."
//            + "${rhelVersion}.x86_64-install.tar.gz";
//
//        OperatingSystemArch osArch = SystemUtil.getOsArch();
//        OperatingSystemFamily osFamily = SystemUtil.getOsFamily();
//        OperatingSystemName osName = SystemUtil.getOsName();
//        System.out.println(osArch + ", " + osFamily + ", " + osName);
//
//        parameters.clear();
//        parameters.put(KEY_REPO_BASE, "http://truss/builds/InternalBuilds");
//        parameters.put(KEY_BUILD_NUMBER, "000031");
//        parameters.put(KEY_BUILD_ID, "99.99.0.sys");
//        parameters.put(KEY_PRODUCT, "tim");
//
//        if (osName.equals(OperatingSystemName.RHEL5)) {
//            parameters.put("rhelVersion", "el5");
//            parameters.put(KEY_CODE_NAME, "99.99.sys_APM_Release.TIM_RedHat_5.5_x64");
//            parameters.put(KEY_BUILD_SUFFIX,
//                "tim-99.99.0.sys.32-cffc82802b8afe3eddc656eb1651d5a8df276c27");
//        } else if (osName.equals(OperatingSystemName.RHEL6)) {
//            parameters.put("rhelVersion", "el6");
//            parameters.put(KEY_CODE_NAME, "99.99.sys_APM_Release.TIM_RedHat_6.0_x64");
//            parameters.put(KEY_BUILD_SUFFIX, "34-cffc82802b8afe3eddc656eb1651d5a8df276c27");
//        }
//        am.fetchTruss(spec, new File("C:\\Foo\\tmp"), parameters);
//
//    }


}
