package com.ca.apm.systemtest.fld.plugin.downloadMethod;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.ArchiveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.common.ACFileUtils;
import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManager;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManagerException;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

/**
 * Created by shadm01 on 07-Jul-15.
 */
@Component("TrussDownloadMethod")
public class TrussDownloadMethodImpl extends AbstractDownloadMethod implements TrussDownloadMethod {
    private static final Logger L = LoggerFactory.getLogger(TrussDownloadMethodImpl.class);
    private static final String NO_INSTALL_TRUSS_SPEC = String
        .format("${%s}/${%s}/build-${%s}(${%s})/${%s}${%s}/${%s}${%s}${%s}.${%s}.${%s}",
            ArtifactManager.KEY_REPO_BASE,
            ArtifactManager.KEY_CODE_NAME,
            ArtifactManager.KEY_BUILD_NUMBER,
            ArtifactManager.KEY_BUILD_ID,
            ArtifactManager.KEY_PRODUCT,
            ArtifactManager.KEY_BUILD_ID,
            ArtifactManager.KEY_INSTALLER_PREFIX,
            ArtifactManager.KEY_BUILD_ID,
            ArtifactManager.KEY_APP_SERVER,
            ArtifactManager.KEY_OS_ARCHITECTURE,
            ArtifactManager.KEY_OS_ARCHIVE_EXTENSION);
    private static final Pattern TRUSS_5 = Pattern.compile("truss:(.+):(.+):(.+):legacy");
    private static final Pattern TRUSS_4 = Pattern.compile("truss:(.+):(.+):(.+):(.+)");
    private static final Pattern TRUSS_3 = Pattern.compile("truss:(.+):(.+):(.+)");
    private static final Pattern TRUSS_2 = Pattern.compile("truss:(.+):(.+)");
    private static final Pattern TRUSS_VERSION = Pattern.compile("(\\d+)\\.(\\d+)\\.(.*)");
    final String SPECIFICATION_LINE
        = "${repo_base}/${code_name}/build-${build_number}(${build_id})"
        + "/${product}${build_id}/${installer_prefix}${build_id}${app_server}.${architecture}"
        + ".${archive_ext}";
    private final String KEY_BUILD_ID = "build_id";

    public TrussDownloadMethodImpl() {
        // TODO move to agent config, or server itself
        baseMap.put("default", "truss.ca.com");
        String[] emea = new String[]{
            "99.99.appmap", "99.99.appmapst", "99.99.aquarius", "99.99.cetusBugs", "99.99.coda",
            "99.99.crux", "99.99.hydra",
            "indus", "java8upgrade", "99.99.pg", "99.99.saml"
        };
        String[] scx = new String[]{
            //"99.99.sys", 
            "10.1.fld", "99.99.dev"
        };
        for (String branch : emea) {
            baseMap.put(branch, "truss-emea-cz");
        }
        for (String branch : scx) {
            baseMap.put(branch, "truss-na-scx");
        }

        String newSpec =
            String.format("${%s}/${%s}/build-${%s}(${%s})/${%s}${%s}/${%s}${%s}${%s}.${%s}",
                ArtifactManager.KEY_REPO_BASE,
                ArtifactManager.KEY_CODE_NAME,
                ArtifactManager.KEY_BUILD_NUMBER,
                ArtifactManager.KEY_BUILD_ID,
                ArtifactManager.KEY_PRODUCT,
                ArtifactManager.KEY_BUILD_ID,
                ArtifactManager.KEY_INSTALLER_PREFIX,
                ArtifactManager.KEY_BUILD_ID,
                ArtifactManager.KEY_OS_ARCHITECTURE,
                ArtifactManager.KEY_OS_ARCHIVE_EXTENSION
            );
        setDefaultAgentSpecification(newSpec);
    }

    private static ParsedSpec parseSpec(CharSequence spec) {
        String branch = null;
        String buildNumber = null;
        String version = null;
        String bittness = null;
        boolean legacy = false;

        Matcher matcher;

        matcher = TRUSS_5.matcher(spec);
        if (matcher.matches()) {
            branch = matcher.group(1);
            buildNumber = matcher.group(2);
            version = matcher.group(3);
            return new ParsedSpec(branch, buildNumber, version, bittness, true);
        }

        matcher = TRUSS_4.matcher(spec);
        if (matcher.matches()) {
            branch = matcher.group(1);
            buildNumber = matcher.group(2);
            version = matcher.group(3);
            bittness = matcher.group(4);
            return new ParsedSpec(branch, buildNumber, version, bittness, legacy);
        }

        matcher = TRUSS_3.matcher(spec);
        // full specification - truss:branch:build:version
        if (matcher.matches()) {
            branch = matcher.group(1);
            buildNumber = matcher.group(2);
            version = matcher.group(3);
            return new ParsedSpec(branch, buildNumber, version, bittness, legacy);
        }


        matcher = TRUSS_2.matcher(spec);
        if (!matcher.matches()) {
            ErrorUtils.logErrorAndReturnException(L, "Invalid truss specification");
        }
        branch = matcher.group(1);
        buildNumber = matcher.group(2);
        // add the "0" automagically with some more regex goodness
        matcher = TRUSS_VERSION.matcher(branch);
        if (matcher.matches()) {
            version = matcher.group(1) + "." + matcher.group(2) + ".0." + matcher.group(3);
        }

        return new ParsedSpec(branch, buildNumber, version, bittness, legacy);
    }

    private static String replacePlaceHolders(String str, Map<String, Object> parameters) {
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            Object obj = entry.getValue();
            if (obj != null) {
                String value = obj.toString();
                str = str.replaceAll("\\$\\{" + key + "\\}", value);
            }
        }
        return str;
    }

    @Deprecated
    Map<String, Object> createDownloaderMap(final String trussServer, final String codeName,
        final String buildId, final String buildNumber,
        final SystemUtil.OperatingSystemFamily platform) {
        @SuppressWarnings("serial")
        Map<String, Object> parameters = new HashMap<String, Object>() {
            {
                put(ArtifactManager.KEY_REPO_BASE,
                    "http://" + (trussServer != null ? trussServer : "truss.ca.com")
                        + "/builds/InternalBuilds");
                put(ArtifactManager.KEY_CODE_NAME, codeName);
                put(ArtifactManager.KEY_BUILD_NUMBER, buildNumber);
                put(ArtifactManager.KEY_BUILD_ID, buildId);
                put(ArtifactManager.KEY_PRODUCT, "introscope");
                put(ArtifactManager.KEY_INSTALLER_PREFIX, "IntroscopeAgentInstaller");
                if (platform == SystemUtil.OperatingSystemFamily.Windows) {
                    put(ArtifactManager.KEY_OS_ARCHITECTURE, "windows");
                    put(ArtifactManager.KEY_OS_ARCHIVE_EXTENSION, "zip");
                } else if (platform == SystemUtil.OperatingSystemFamily.Linux) {
                    put(ArtifactManager.KEY_OS_ARCHITECTURE, "unix");
                    put(ArtifactManager.KEY_OS_ARCHIVE_EXTENSION, "tar");
                } else {
                    put(ArtifactManager.KEY_OS_ARCHITECTURE, "zOS");
                    put(ArtifactManager.KEY_OS_ARCHIVE_EXTENSION, "tar");
                }

            }
        };
        return parameters;
    }

    @Override
    public Path downloadAgent(final String trussServer,
        final String codeName, final String buildId, final String buildNumber,
        final SystemUtil.OperatingSystemFamily platform) {
        Path installerDir = null;
        try {
            installerDir = Files
                .createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "staging");
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrap(L, e, "Cannot create installer directory");
        }

        L.info("Fetch and unzip installation files into {}", installerDir.toString());
        try {
            Map<String, Object> parameters = createDownloaderMap(trussServer, codeName, buildId,
                buildNumber, platform);

            ArtifactFetchResult installerPackage = fetch(DEFAULT_AGENT_SPECIFICATION,
                installerDir.toFile(), parameters, true);
            ACFileUtils.unpackFile(installerPackage.getFile(), installerPackage.getFile().getParentFile());
            Files.delete(installerPackage.getFile().toPath());
        } catch (ArtifactManagerException e) {
            throw ErrorUtils.logExceptionAndWrap(L, e, "Cannot download installer artifact");
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrap(L, e,
                "Cannot delete unpacked installer artifact");
        } catch (ArchiveException e) {
            throw ErrorUtils.logExceptionAndWrap(L, e, "Cannot unpack installer artifact");
        }
        return installerDir;
    }

    @Override
    public String getDownloadUrl(String noInstallerSpecification,
        String appServer) throws ArtifactManagerException {
        ParsedSpec spec = parseSpec(noInstallerSpecification);

        String branch = spec.branch;
        String buildNumber = spec.buildNumber;
        String version = spec.version;
        String bittness = spec.bittness;
        String noInstallTrussSpec = NO_INSTALL_TRUSS_SPEC;
        String trussServer = findBase(branch);
        String codeName = branch + "-ISCP";
        String prefix = "IntroscopeAgentFiles-NoInstaller";
        appServer = appServer.toLowerCase();

        Map<String, Object> trussMap = createDownloaderMap(trussServer, codeName, version,
            buildNumber, SystemUtil.getOsFamily());
        trussMap.put(ArtifactManager.KEY_INSTALLER_PREFIX, prefix);
        trussMap.put(ArtifactManager.KEY_APP_SERVER, appServer);

        noInstallTrussSpec = defaultIfBlank(noInstallTrussSpec, getDefaultAgentSpecification());
        noInstallTrussSpec = replacePlaceHolders(noInstallTrussSpec, trussMap);

        L.info("Result URL: {}", noInstallTrussSpec);

        return noInstallTrussSpec;
    }

    @Override
    public ArtifactFetchResult fetchResultFromDownloadSource(String noInstallerSpecification,
        File tmpDirectory, String appServerName) {
        ArtifactFetchResult result = null;

        ParsedSpec spec = parseSpec(noInstallerSpecification);

        String branch = spec.branch;
        String buildNumber = spec.buildNumber;
        String version = spec.version;
        String bittness = spec.bittness;
        Boolean legacy = spec.legacy;
        String noInstallTrussSpec = NO_INSTALL_TRUSS_SPEC;
        String trussServer = findBase(branch);
        String codeName = branch + "-ISCP";
        String prefix;
        if (legacy) {
            prefix = "IntroscopeAgentFiles-Legacy-NoInstaller";
        } else {
            prefix = "IntroscopeAgentFiles-NoInstaller";
        }
        String appServer = appServerName.toLowerCase();

        Map<String, Object> trussMap = createDownloaderMap(trussServer, codeName, version,
            buildNumber, SystemUtil.getOsFamily());
        trussMap.put(ArtifactManager.KEY_INSTALLER_PREFIX, prefix);
        trussMap.put(ArtifactManager.KEY_APP_SERVER, appServer);
        try {
            result = fetch(noInstallTrussSpec, tmpDirectory, trussMap, true);
        } catch (ArtifactManagerException e) {
            throw ErrorUtils.logExceptionAndWrap(L, e,
                "Error installing agent - could not download no-installer archive from Truss");
        }

        return result;
    }

    /**
     * Fetches an artifact from Truss.  The artifactSpecification should be supplied by the
     * plugin, which will
     * know the required parameters and Truss layout.  The downloader will perform key
     * substitution on the specification to
     * create the download URL
     *
     * @param artifactSpecification The URL specifying the download. If null, defaults to
     *                              DEFAULT_TRUSS_SPECIFICATION
     * @param destinationDirectory  The artifact will be downloaded to this directory
     * @param parameters            Map containing the parameters for key/value substitution in
     *                              the artifactSpecification.
     * @param useCache              Use cache (or download directly from server)
     * @return Artifact fetch result
     * @throws ArtifactManagerException
     */
    @Override
    public ArtifactFetchResult fetch(String artifactSpecification, File destinationDirectory,
        Map<String, Object> parameters, boolean useCache) throws ArtifactManagerException {

        if (artifactSpecification == null) {
            artifactSpecification = getDefaultAgentSpecification();
        }
        if (parameters == null) {
            final String msg = "Missing parameters specification";
            L.error(msg);
            throw new ArtifactManagerException(msg);
        }

        artifactSpecification = replacePlaceHolders(artifactSpecification, parameters);
        L.info("Using URL: {}", artifactSpecification);

        ArtifactFetchResult result = super
            .fetch(artifactSpecification, destinationDirectory, useCache);
        result.setBuildId(parameters.get(KEY_BUILD_ID).toString());

        L.info("Download Complete");

        return result;
    }

    public int getFetchUrlSize(String artifactSpecification,
        Map<String, Object> parameters) throws ArtifactManagerException, IOException {

        if (artifactSpecification == null) {
            artifactSpecification = getDefaultAgentSpecification();
        }
        if (parameters == null) {
            final String msg = "Missing parameters specification";
            L.error(msg);
            throw new ArtifactManagerException(msg);
        }

        artifactSpecification = replacePlaceHolders(artifactSpecification, parameters);
        URL artifactUrl = new URL(artifactSpecification);
        HttpURLConnection conn = (HttpURLConnection) artifactUrl.openConnection();

        return conn.getContentLength();
    }

    public ArtifactFetchResult fetch(String downloadUrl, File destinationDirectory,
        boolean useCache) throws ArtifactManagerException {
        L.info("Downloading using Truss download Method");

        ArtifactFetchResult result = super.fetch(downloadUrl, destinationDirectory, useCache);

        L.info("Download Complete");
        return result;
    }

    private static class ParsedSpec {
        String branch = null;
        String buildNumber = null;
        String version = null;
        String bittness = null;
        boolean legacy = false;

        public ParsedSpec(String branch, String buildNumber, String version, String bittness, boolean legacy) {
            this.branch = branch;
            this.buildNumber = buildNumber;
            this.version = version;
            this.bittness = bittness;
            this.legacy = legacy;
        }
    }


}
