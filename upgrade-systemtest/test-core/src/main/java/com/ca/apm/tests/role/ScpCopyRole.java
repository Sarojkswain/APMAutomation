/*
 * Copyright (c) 2016 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.tests.role;


import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.xfer.FileSystemFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.text.MessageFormat;

public class ScpCopyRole extends AbstractRole {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScpCopyRole.class);

    private final Path file;
    private final String user;
    private final String host;
    private final int port;
    private final String destDir;

    public ScpCopyRole(String roleId, Path file, String host) {
        this(roleId, file, "root", host, "/opt/automation/deployed", 22);
    }

    public ScpCopyRole(String roleId, Path file, String user, String host, String destDir, int port) {
        super(roleId);
        this.file = file;
        this.user = user;
        this.host = host;
        this.port = port;
        this.destDir = destDir + "/";
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        try (SSHClient ssh = new SSHClient()) {

            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.connect(host, port);
            ssh.authPassword(user, "Lister@123");

            File srcFile = file.toFile();
            SFTPClient sftpClient = ssh.newSFTPClient();
            String destFile = destDir + srcFile.getName();
            sftpClient.mkdirs(destDir);
            sftpClient.put(new FileSystemFile(srcFile), destFile);
        } catch (Exception e) {
            final String msg = MessageFormat.format(
                    "Failed to upload {1} to {2}@{3}:{4}:{5}. Exception: {0}",
                    e, file, "root", host, port, destDir);
            LOGGER.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public String getDestFile() {
        return destDir + file.getFileName();
    }
}
