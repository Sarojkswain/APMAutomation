package com.ca.apm.systemtest.fld.role;

import com.ca.apm.systemtest.fld.flow.SslCertsAndKeysFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;

import static com.ca.apm.systemtest.fld.flow.SslCertsAndKeysFlowContext.KeyFileEntry
    .KeyFileFormat.DER;
import static com.ca.apm.systemtest.fld.flow.SslCertsAndKeysFlowContext.KeyFileEntry
    .KeyFileFormat.PEM;

/**
 * @author haiva01
 */
public class SslCertsAndKeysRole extends AbstractRole {

    protected SslCertsAndKeysFlowContext sslCertsAndKeysFlowContext;

    protected SslCertsAndKeysRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        sslCertsAndKeysFlowContext = builder.sslCertsAndKeysFlowContext;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        runFlow(aaClient, sslCertsAndKeysFlowContext);
    }

    public static class Builder extends BuilderBase<Builder, SslCertsAndKeysRole> {
        private final Logger log = LoggerFactory.getLogger(Builder.class);

        protected String roleId;

        protected SslCertsAndKeysFlowContext.Builder sslCertsAndKeysFlowContextBuilder
            = new SslCertsAndKeysFlowContext.Builder();
        protected SslCertsAndKeysFlowContext sslCertsAndKeysFlowContext;

        public Builder(String roleId) {
            this.roleId = roleId;
        }

        public Builder setKeyStorePath(String keyStorePath, String password) {
            sslCertsAndKeysFlowContextBuilder.setKeyStorePath(keyStorePath);
            sslCertsAndKeysFlowContextBuilder.setKeyStorePassword(password);
            return builder();
        }

        public Builder addCertToKeystore(String alias, X509Certificate certificate) {
            sslCertsAndKeysFlowContextBuilder.addCertToKeystore(alias, certificate);
            return builder();
        }

        public Builder writeCertToPemFile(String filePath, X509Certificate certificate) {
            sslCertsAndKeysFlowContextBuilder.writeCertToPemFile(filePath, certificate);
            return builder();
        }


        public Builder addPrivateKeyToKeystore(String alias, PrivateKey privateKey, String password,
            List<X509Certificate> certChain) {
            sslCertsAndKeysFlowContextBuilder
                .addPrivateKeyToKeystore(alias, privateKey, password, certChain);
            return builder();
        }

        public Builder writePrivateKeyToPemFile(String filePath, PrivateKey privateKey) {
            sslCertsAndKeysFlowContextBuilder.writeKeyToPemFile(PEM, filePath, privateKey, true);
            return builder();
        }

        public Builder writePublicKeyToPemFile(String filePath, PublicKey publicKey) {
            sslCertsAndKeysFlowContextBuilder.writeKeyToPemFile(PEM, filePath, publicKey, false);
            return builder();
        }

        public Builder writePrivateKeyToDerFile(String filePath, PrivateKey privateKey) {
            sslCertsAndKeysFlowContextBuilder.writeKeyToPemFile(DER, filePath, privateKey, true);
            return builder();
        }

        public Builder writePublicKeyToDerFile(String filePath, PublicKey publicKey) {
            sslCertsAndKeysFlowContextBuilder.writeKeyToPemFile(DER, filePath, publicKey, false);
            return builder();
        }

        @Override
        protected SslCertsAndKeysRole getInstance() {
            return new SslCertsAndKeysRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public SslCertsAndKeysRole build() {
            sslCertsAndKeysFlowContext = sslCertsAndKeysFlowContextBuilder.build();
            return getInstance();
        }
    }
}
