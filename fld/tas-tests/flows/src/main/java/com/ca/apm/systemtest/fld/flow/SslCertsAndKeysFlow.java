package com.ca.apm.systemtest.fld.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.systemtest.fld.flow.SslCertsAndKeysFlowContext.CertEntry;
import com.ca.apm.systemtest.fld.flow.SslCertsAndKeysFlowContext.CertFileEntry;
import com.ca.apm.systemtest.fld.flow.SslCertsAndKeysFlowContext.KeyFileEntry;
import com.ca.apm.systemtest.fld.flow.SslCertsAndKeysFlowContext.PrivateKeyEntry;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.util.Collection;

import static com.ca.apm.systemtest.fld.util.SslCertsAndKeysUtils.writeKeyDer;
import static com.ca.apm.systemtest.fld.util.SslCertsAndKeysUtils.writeKeyPem;
import static com.ca.apm.systemtest.fld.util.SslCertsAndKeysUtils.writeX509CertificatePem;

/**
 * @author haiva01
 */
@Flow
public class SslCertsAndKeysFlow extends FlowBase {
    private static final Logger log = LoggerFactory.getLogger(SslCertsAndKeysFlow.class);

    @FlowContext
    private SslCertsAndKeysFlowContext context;

    @Override
    public void run() throws Exception {
        KeyStore javaCertsStore = KeyStore.getInstance("JKS");
        final File keyStoreFile = new File(context.getKeyStorePath());
        log.info("Loading keystore from {}", keyStoreFile.getAbsolutePath());
        try (InputStream input = FileUtils.openInputStream(keyStoreFile)) {
            javaCertsStore.load(input, context.getKeyStorePassword().toCharArray());
        }

        log.info("Updating keystore...");
        Collection<CertEntry> certEntries = context.getCertificates();
        for (CertEntry certEntry : certEntries) {
            log.info("Adding certificate with alias \"{}\"", certEntry.alias);
            javaCertsStore.setCertificateEntry(certEntry.alias, certEntry.certificate);
        }

        Collection<PrivateKeyEntry> privateKeyEntries = context.getPrivateKeys();
        for (PrivateKeyEntry pkEntry : privateKeyEntries) {
            log.info("Adding private key with alias \"{}\"", pkEntry.alias);
            javaCertsStore
                .setKeyEntry(pkEntry.alias, pkEntry.privateKey, pkEntry.password.toCharArray(),
                    pkEntry.certChain);
        }

        FileUtils.deleteQuietly(keyStoreFile);
        log.info("Storing updated keystore to {}", keyStoreFile.getAbsolutePath());
        try (OutputStream out = FileUtils.openOutputStream(keyStoreFile)) {
            javaCertsStore.store(out, context.getKeyStorePassword().toCharArray());
        }

        for (CertFileEntry certFileEntry : context.getCertificateFiles()) {
            writeX509CertificatePem(new File(certFileEntry.filePath), "certificate",
                certFileEntry.certificate);
        }

        for (KeyFileEntry pkEntry : context.getKeyFiles()) {
            final String keyName = (pkEntry.privateKey ? "private" : "public") + " key as in "
                + pkEntry.format + " format";
            switch (pkEntry.format) {
                case PEM:
                    writeKeyPem(new File(pkEntry.filePath), keyName, pkEntry.key);
                    break;
                case DER:
                    writeKeyDer(new File(pkEntry.filePath), keyName, pkEntry.key);
                    break;
            }
        }
    }
}
