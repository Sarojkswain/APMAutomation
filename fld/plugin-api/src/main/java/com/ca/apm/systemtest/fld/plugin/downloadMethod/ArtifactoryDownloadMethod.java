package com.ca.apm.systemtest.fld.plugin.downloadMethod;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;

import com.ca.apm.systemtest.fld.common.ACFileUtils;
import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.ProcessUtils;
import com.ca.apm.systemtest.fld.common.ProcessUtils2;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManagerException;
import com.ca.apm.systemtest.fld.plugin.downloader.MavenSpecification;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil;

/**
 * Created by shadm01 on 07-Jul-15.
 */
@Component("ArtifactoryDownloadMethod")
public class ArtifactoryDownloadMethod extends AbstractDownloadMethod {
    private static final Logger L = LoggerFactory.getLogger(ArtifactoryDownloadMethod.class);
    private static final String MVN_BAT = "mvn.bat";
    private static final String MVN_CMD = "mvn.cmd";

    //---------------------------^^^^ FROM TRUSS METHOD ^^^----------------------------------------

    /**
     * This function builds artifact coordinates string, usable for
     * ArtifactManager.fetchArtifact() with RepositoryType.ARTIFACTORY argument,
     * from its parts.
     *
     * @param groupId    artifact group.
     * @param artifactId artifact ID
     * @param version    artifact version
     * @param classifier artifact classifier (can be null)
     * @param type       artifact type (can be null)
     * @return artifact coordinates as String
     */
    public static String buildArtifactCoordinatesStr(String groupId, String artifactId,
        String version, String classifier, String type) throws RuntimeException {
        if (L.isDebugEnabled()) {
            L.debug("Artifact coordinates: {}:{}:{}:{}:{}", groupId, artifactId, version,
                classifier, type);
        }

        final String paramMsg = "The {0} parameter must not be null.";
        if (groupId == null) {
            throw ErrorUtils.logErrorAndReturnException(L, paramMsg, "groupId");
        }
        if (artifactId == null) {
            throw ErrorUtils.logErrorAndReturnException(L, paramMsg, "artifactId");
        }
        if (version == null) {
            throw ErrorUtils.logErrorAndReturnException(L, paramMsg, "version");
        }

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

    private static Path findMvnBatOnWindows() throws ArtifactManagerException {
        final FileSystem fs = FileSystems.getDefault();

        // First try to find appropriate batch file using M2_HOME environment variable.
        // This is necessary to avoid issues when PATH contains one Maven installation
        // but M2_HOME points to another.

        List<Path> mvnBatCandidates = new ArrayList<>(4);

        final String m2HomeEnv = System.getenv("M2_HOME");
        if (m2HomeEnv != null) {
            Path mvnBin = fs.getPath(m2HomeEnv, "bin");
            mvnBatCandidates.add(mvnBin.resolve(MVN_CMD).toAbsolutePath());
            mvnBatCandidates.add(mvnBin.resolve(MVN_BAT).toAbsolutePath());
        }

        for (Path candidate : mvnBatCandidates) {
            L.debug("Checking if {} exists.", candidate.toString());
            if (java.nio.file.Files.exists(candidate)) {
                L.debug("Found {}.", candidate.toString());
                return candidate.toAbsolutePath();
            }
        }

        // If the above has failed, try to find mvn.bat or mvn.cmd on PATH.

        Path mvn = ACFileUtils.findFileInPathEnv("mvn.cmd");
        if (mvn == null) {
            mvn = ACFileUtils.findFileInPathEnv("mvn.bat");
        }
        if (mvn == null) {
            throw new ArtifactManagerException(
                "Neither mvn.cmd nor mvn.bat could be found anywhere.");
        }

        mvn = mvn.toAbsolutePath();
        L.debug("Found {}.", mvn.toString());
        return mvn;
    }

    @Override
    @Deprecated
    public Path downloadAgent(String trussServer, String codeName, String buildId,
        String buildNumber, SystemUtil.OperatingSystemFamily platform) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public ArtifactFetchResult fetchResultFromDownloadSource(String noInstallerSpecification,
        File tmpDirectory, String appServerName) {
        throw new UnsupportedOperationException();
    }

    /**
     * This function donwloads file into temporary directory
     *
     * @param url            repository url
     * @param groupId        artifact group.
     * @param artifactId     artifact ID
     * @param version        artifact version
     * @param classifier     artifact classifier (can be null)
     * @param type           artifact type (can be null)
     * @param downloadFolder folder, where to download artifact (if null, temp folder is selected)
     * @return ArtifactFetchResult - result of download
     */
    @ExposeMethod(
        description = "Fetches an artifact from Artifactory to [user.dir]/temp/[tempDirName]")
    public ArtifactFetchResult fetchTempArtifact(String url, String groupId, String artifactId,
        String version, String classifier, String type, String downloadFolder) {
        String artifactCoords = buildArtifactCoordinatesStr(groupId, artifactId, version,
            classifier, type);

        File downloadDir;
        if (downloadFolder == null) {
            downloadDir = new File(System.getProperty("java.io.tmpdir"));
        } else {
            downloadDir = new File(downloadFolder);
        }

        ArtifactFetchResult fetchRes;
        try {
            fetchRes = fetchUsingMavenCommandLine(artifactCoords, downloadDir, url);
        } catch (ArtifactManagerException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(L, e,
                "Failed to fetch artifact from artifactory. Exception {0}");
        }

        return fetchRes;
    }

    @ExposeMethod(
        description = "Fetches an artifact from Artifactory to System system temp directory")
    public ArtifactFetchResult fetchTempArtifact(String url, String groupId, String artifactId,
        String version, String classifier, String type) {
        return fetchTempArtifact(url, groupId, artifactId, version, classifier, type, null);
    }

    /**
     * Fetches an artifact from the maven repository / artifactory by running "mvn
     * dependency:get" in a process.
     *
     * @param artifactSpecification - one line artifact description
     * @param destinationDirectory  - path, where artifact will be downloaded
     * @param repositoryUrl         - url of repository
     * @return Artifact
     * @throws ArtifactManagerException
     */

    public ArtifactFetchResult fetchUsingMavenCommandLine(final String artifactSpecification,
        final File destinationDirectory,
        final String repositoryUrl) throws ArtifactManagerException {
        if (artifactSpecification == null) {
            final String msg = "Missing artifact specification";
            L.error(msg);
            throw new ArtifactManagerException(msg);
        }

        Path path = null;
        try {
            path = Files.createTempDirectory("lo-agent");
        } catch (IOException e1) {
            throw new ArtifactManagerException(e1);
        }

        ArtifactFetchResult result = new ArtifactFetchResult();

        // TODO - the "-Ddest" parameter will be deprecated in the maven plugin, will need to
        // re-evaluate, or stick to an older version.  An alternate strategy can be to create
        // simple pom.xml in a temp directory, and use
        // it to download the artifact.  In any case, the use of maven is preferred, since it
        // understands things like snapshots, etc
        ArrayList<String> commands = new ArrayList<>(10);
        SystemUtil.OperatingSystemFamily osFamily = SystemUtil.getOsFamily();
        if (osFamily.equals(SystemUtil.OperatingSystemFamily.Windows)) {
            Path mvn = findMvnBatOnWindows();
            commands.add(mvn.toString());
        } else {
            commands.add("mvn");
        }

        commands.add("dependency:get");
        commands.add("-Dartifact=" + artifactSpecification);

        if (repositoryUrl != null && repositoryUrl.length() > 0) {
            commands.add("-DremoteRepositories=" + repositoryUrl);
        }
        commands.add("-Dtransitive=false");
        commands.add("\"-Ddest=" + path + "\"");

        Map<String, String> env = new TreeMap<>(System.getenv());
        env.put("CLASSPATH", null);

        ProcessExecutor pe = ProcessUtils2.newProcessExecutor()
            .command(commands)
            .directory(path.toFile())
            .environment(env);
        int exitCode = -9999999;
        try {
            StartedProcess process = ProcessUtils2.startProcess(pe);
            exitCode = ProcessUtils.waitForProcess(process.getProcess(), 1, TimeUnit.HOURS, true);

            if (exitCode == 0) {
                // If the maven dependency:get was successful, look for the file in the temp
                // directory
                File[] files = path.toFile().listFiles();
                if (L.isDebugEnabled()) {
                    L.debug("list of possible artifact files: {}", (Object[]) files);
                }

                String filename = null;
                File file = null;
                for (File f : files) {
                    filename = f.getName();
                    if (filename.equals(".") || filename.equals("..")) {
                        filename = null;
                        continue;
                    }
                    file = f;
                    if (L.isDebugEnabled()) {
                        L.debug("Found file {}.", file);
                    }

                    break;
                }

                // if file found, move it to our destination directory
                if (file == null) {
                    throw new ArtifactManagerException(
                        "Did not find resolved artifact for " + artifactSpecification);
                }

                CopyOption[] options = new CopyOption[]{StandardCopyOption.REPLACE_EXISTING};
                Path src = Paths.get(file.getAbsolutePath());
                Path tgt = Paths
                    .get(destinationDirectory.getAbsolutePath() + File.separator + filename);
                Files.move(src, tgt, options);
                Files.delete(path);

                MavenSpecification spec = new MavenSpecification(artifactSpecification);
                result.setBuildId(spec.getVersion());
                result.setFile(new File(destinationDirectory, filename));
            }
        } catch (Exception e) {
            final String msg = MessageFormat
                .format("Failed to download {0}. Exception: {1}", artifactSpecification,
                    e.getMessage());
            L.error(msg, e);
            throw new ArtifactManagerException(e);
        }

        return result;
    }


}
