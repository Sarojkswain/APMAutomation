package com.ca.apm.systemtest.fld.plugin.downloadMethod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilder;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManagerException;
import com.ca.apm.systemtest.fld.plugin.downloader.maven.Booter;
import com.ca.apm.systemtest.fld.plugin.downloader.maven.ManualRepositorySystemFactory;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil;

/**
 * Created by shadm01 on 10-Jul-15.
 */

@Component("ArtifactoryLiteDownloadMethod")
public class ArtifactoryLiteDownloadMethodImpl extends AbstractDownloadMethod
    implements ArtifactoryLiteDownloadMethod {
    private static final Logger L = LoggerFactory.getLogger(
        ArtifactoryLiteDownloadMethodImpl.class);

    @SuppressWarnings("checkstyle:constantname")
    public static final String userHome = System.getProperty("user.home");

    @SuppressWarnings("checkstyle:constantname")
    public static final File userMavenConfigurationHome = new File(userHome, ".m2");

    @SuppressWarnings("checkstyle:constantname")
    public static final String envM2Home = System.getenv("M2_HOME");

    public static final File DEFAULT_USER_SETTINGS_FILE = new File(userMavenConfigurationHome, "settings.xml");
    public static final File DEFAULT_GLOBAL_SETTINGS_FILE = new File(System.getProperty("maven.home", envM2Home != null ? envM2Home : ""), "conf/settings.xml");

    @Override
    public Path downloadAgent(final String artifactoryServer, final String codeName, final String buildId,
                              final String buildNumber, final SystemUtil.OperatingSystemFamily platform) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArtifactFetchResult fetchResultFromDownloadSource(String noInstallerSpecification, File tmpDirectory, String appServerName) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param artifSpecification - string containing: groupId, artifactId, version, ?type, ?classifier
     * @param destinationDirectory - folder where to download artifact
     * @param repositoryUrl - repo url
     * @param useCache - use cache (not used actually)
     * @return Artifact
     * @throws ArtifactManagerException
     */

    public ArtifactFetchResult fetch(String artifSpecification, File destinationDirectory, String repositoryUrl, boolean useCache) throws ArtifactManagerException {

        // Parse string to Artifact
        Artifact artifactSpec = parseArtifactCoordinatesToArtifact(artifSpecification);

        // Make temporary working directory.

        Path path;
        try {
            path = Files.createTempDirectory("lo-agent");
        } catch (IOException e1) {
            throw new ArtifactManagerException(e1);
        }

        // Read Maven settings.

        SettingsBuildingRequest settingsBuildingRequest = new DefaultSettingsBuildingRequest();
        settingsBuildingRequest.setSystemProperties(System.getProperties());
        settingsBuildingRequest.setUserSettingsFile(DEFAULT_USER_SETTINGS_FILE);
        settingsBuildingRequest.setGlobalSettingsFile(DEFAULT_GLOBAL_SETTINGS_FILE);

        SettingsBuildingResult settingsBuildingResult;
        DefaultSettingsBuilderFactory mvnSettingBuilderFactory
                = new DefaultSettingsBuilderFactory();
        DefaultSettingsBuilder settingsBuilder = mvnSettingBuilderFactory.newInstance();
        try {
            settingsBuildingResult = settingsBuilder.build(settingsBuildingRequest);
        } catch (SettingsBuildingException e) {
            final String msg = MessageFormat.format("Failed to fetch artifact {1}. Exception: {0}",
                    e.getMessage(), artifactSpec);
            L.error(msg, e);
            throw new ArtifactManagerException(msg, e);
        }

        // Parse Maven profiles for repositories.

        Settings effectiveSettings = settingsBuildingResult.getEffectiveSettings();

        L.debug("active profiles: {}", effectiveSettings.getActiveProfiles());
        Map<String, Profile> profilesMap = effectiveSettings.getProfilesAsMap();

        Collection<RemoteRepository> remotes = new ArrayList<>(20);

        String repoBase = repositoryUrl; //TODO - DM - URL fix
        if (repoBase != null && repoBase.length() > 0) {
            remotes.add(
                    new RemoteRepository.Builder("apm-third-party-isl", "default", repoBase).build()
            );
        }
        Object additionalRepositorisRef = super.mirror; //TODO - DM - mirrors
        if (additionalRepositorisRef != null) {
            @SuppressWarnings("unchecked")
            Collection<RemoteRepository> additionalRemoteRepositories
                    = (Collection<RemoteRepository>) additionalRepositorisRef;
            remotes.addAll(additionalRemoteRepositories);
        }

        for (String profileName : effectiveSettings.getActiveProfiles()) {
            L.debug("examining profile {}", profileName);
            Profile profile = profilesMap.get(profileName);
            List<Repository> repositories = profile.getRepositories();
            for (Repository repo : repositories) {
                RemoteRepository remoteRepo
                        = new RemoteRepository.Builder(repo.getId(), "default", repo.getUrl()).build();
                remotes.add(remoteRepo);
            }
        }

        // Get the actual artifact.

        RepositorySystem system = ManualRepositorySystemFactory.newRepositorySystem();
        RepositorySystemSession session = Booter.newRepositorySystemSession(system);

        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact(artifactSpec);
        artifactRequest.setRepositories(new ArrayList<>(remotes));

        ArtifactResult artifactResult;
        try {
            artifactResult = system.resolveArtifact(session, artifactRequest);
            L.info("Artifact retrieved successfully");
        } catch (ArtifactResolutionException e) {
            final String msg = MessageFormat.format("Failed to fetch artifact {1}. Exception: {0}",
                    e.getMessage(), artifactSpec);
            L.error(msg, e);
            throw new ArtifactManagerException(msg, e);
        }

        // Populate result.

        Artifact artifact = artifactResult.getArtifact();
        ArtifactFetchResult result = new ArtifactFetchResult();
        result.setBuildId(artifact.getVersion());
        L.debug("Downloaded file: {}", artifact.getFile());

        try {
            File destFile = new File(destinationDirectory, artifact.getFile().getName());
            // attempt to delete file if it already exists
            if (destFile.exists()) {
                destFile.delete();
            }
            // Move to user provided destination directory.
            FileUtils.moveFileToDirectory(artifact.getFile(), destinationDirectory, true);
            result.setFile(new File(destinationDirectory, artifact.getFile().getName()));
            // Delete temporary directory.
            FileUtils.forceDelete(path.toFile());
        } catch (IOException e) {
            final String msg = MessageFormat.format("Failed to fetch artifact {1}. Exception: {0}",
                    e.getMessage(), artifactSpec);
            L.error(msg, e);
            throw new ArtifactManagerException(msg, e);
        }

        return result;
    }

    /**
     * This function parse artifact coordinates String to Artifact type
     *
     * @param artifactSpecification artifact coordinates String
     * @return artifact as Artifact
     */
    public static Artifact parseArtifactCoordinatesToArtifact(String artifactSpecification) throws RuntimeException {
        if (L.isDebugEnabled()) {
            L.debug("Artifact coordinates String: {}", artifactSpecification);
        }

        final String paramMsg = "The {0} parameter must not be null.";
        if (artifactSpecification == null) {
            throw ErrorUtils.logErrorAndReturnException(L, paramMsg, "artifactSpecification");
        }

        String[] artParts = artifactSpecification.split(":");
        if (artParts.length > 5) {
            throw ErrorUtils.logErrorAndReturnException(L, "Artifact coordinates '{0}' is invalid. It contents too many separators ':' ", artifactSpecification);
        }
        if (artParts.length < 3) {
            throw ErrorUtils.logErrorAndReturnException(L, "Artifact coordinates '{0}' is invalid. It contents less then 3 coordinates", artifactSpecification);
        }

        String groupId = artParts[0];
        String artifactId = artParts[1];
        String version = artParts[2];
        String type = artParts.length > 3 ? artParts[3] : null;
        String classifier = artParts.length > 4 ? artParts[4] : null;

        Artifact artifact = new DefaultArtifact(groupId, artifactId, classifier, type, version);

        return artifact;
    }

    /**
     * Chceks Builds artifactory string from input parameters, separated by ':'

     * @return              - String separated by ':' containing Maven specification
     * @throws RuntimeException
     */

    /**
     *
     * @param url           - Artifactory repository URL
     * @param groupId       - groupID from Maven config
     * @param artifactId    - artifact ID from Maven config
     * @param version       - Version from Maven config
     * @param classifier    - Suffix added to file name - can be null
     * @param type          - File extension
     * @return
     */
    @ExposeMethod(description = "Fetches an artifact from Artifactory to [user.dir]/temp/[tempDirName]")
    @Override
    public ArtifactFetchResult fetchTempArtifact(String url, String groupId, String artifactId, String version, String classifier, String type) {

        String artifactCoords = buildArtifactCoordinatesStr(groupId, artifactId, version, classifier, type);
        final File tempDir = new File(System.getProperty("java.io.tmpdir")); //FIXME - DM - will work on linux ?

        try {
            return fetch(artifactCoords, tempDir, url, true);
        } catch (ArtifactManagerException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(L, e, "Failed to fetch artifact from artifactory. Exception {0}");
        }
    }

    /**
     * Chceks Builds artifactory string from input parameters, separated by ':'
     * @param groupId       - groupID from Maven config
     * @param artifactId    - artifact ID from Maven config
     * @param version       - Version from Maven config
     * @param classifier    - Suffix added to file name - can be null
     * @param type          - File extension
     * @return              - String separated by ':' containing Maven specification
     * @throws RuntimeException
     */
    public static String buildArtifactCoordinatesStr(String groupId, String artifactId,
                                                     String version, String classifier, String type) throws RuntimeException {
        if (L.isDebugEnabled())
            L.debug("Artifact coordinates: {}:{}:{}:{}:{}", groupId, artifactId, version, classifier, type);

        final String paramMsg = "The {0} parameter must not be null.";
        if (groupId == null)
            throw ErrorUtils.logErrorAndReturnException(L, paramMsg, "groupId");
        if (artifactId == null)
            throw ErrorUtils.logErrorAndReturnException(L, paramMsg, "artifactId");
        if (version == null)
            throw ErrorUtils.logErrorAndReturnException(L, paramMsg, "version");

        StringBuilder sb = new StringBuilder(128)
                .append(groupId)
                .append(':')
                .append(artifactId)
                .append(':')
                .append(version);

        // always add ':', even with empty type
        sb.append(':');
        if (type != null) {
            sb.append(type);
        }
        // always add ':', even with empty classifier
        sb.append(':');
        if (classifier != null) {
            sb.append(classifier);
        }

        return sb.toString();
    }
}
