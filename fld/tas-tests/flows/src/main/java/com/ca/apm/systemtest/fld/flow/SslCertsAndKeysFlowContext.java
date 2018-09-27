package com.ca.apm.systemtest.fld.flow;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.flow.AutowireCapable;
import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.flow.SslCertsAndKeysFlowContext.KeyFileEntry.KeyFileFormat;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.property.AbstractEnvPropertySerializer;
import com.ca.tas.property.EnvPropSerializable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.ca.apm.systemtest.fld.util.SslCertsAndKeysUtils.BC_PROVIDER;
import static com.ca.apm.systemtest.fld.util.SslCertsAndKeysUtils.decodeCertificate;
import static com.ca.apm.systemtest.fld.util.SslCertsAndKeysUtils.decodePrivateKey;
import static com.ca.apm.systemtest.fld.util.SslCertsAndKeysUtils.decodePublicKey;

/**
 * @author haiva01
 */
public class SslCertsAndKeysFlowContext implements AutowireCapable,
    EnvPropSerializable<SslCertsAndKeysFlowContext> {
    public static final String DATA = "DATA";
    private static final Logger log = LoggerFactory.getLogger(SslCertsAndKeysFlowContext.class);
    private final transient SslCertsAndKeysFlowContext.Serializer envPropSerializer;

    protected Data data = new Data();

    public SslCertsAndKeysFlowContext() {
        this.envPropSerializer = new Serializer(this);
    }

    public SslCertsAndKeysFlowContext(Data data) {
        this();
        this.data = data;
    }

    public SslCertsAndKeysFlowContext(Builder builder) {
        this();
        data.keyStorePath = builder.keystorePath;
        data.keyStorePassword = builder.keyStorePassword;
        data.certs = builder.dataCertEntries;
        data.privateKeys = builder.dataPrivateKeyEntries;
        data.certFileEntries = builder.dataCertFileEntries;
        data.keyFiles = builder.dataPrivateKeyFileEntries;
    }

    public String getKeyStorePath() {
        return this.data.keyStorePath;
    }

    public String getKeyStorePassword() {
        return this.data.keyStorePassword;
    }

    public Collection<CertEntry> getCertificates() {
        Collection<CertEntry> certEntries = new ArrayList<>(data.certs.length);
        for (DataCertEntry dataCertEntry : data.certs) {
            certEntries
                .add(new CertEntry(dataCertEntry.alias, decodeCertificate(dataCertEntry.cert)));
        }
        return certEntries;
    }

    public Collection<CertFileEntry> getCertificateFiles() {
        Collection<CertFileEntry> certFileEntries = new ArrayList<>(data.certFileEntries.length);
        for (DataCertFileEntry certFileEntry : data.certFileEntries) {
            certFileEntries.add(new CertFileEntry(decodeCertificate(certFileEntry.certificate),
                certFileEntry.filePath));
        }
        return certFileEntries;
    }

    public Collection<PrivateKeyEntry> getPrivateKeys() {
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("RSA", BC_PROVIDER);
        } catch (NoSuchAlgorithmException e) {
            throw ErrorUtils
                .logExceptionAndWrapFmt(log, e, "Failed to create key factory. Exception: {0}");
        }

        Collection<PrivateKeyEntry> privateKeyEntries = new ArrayList<>(data.privateKeys.length);
        for (DataPrivateKeyEntry pkEntry : data.privateKeys) {
            PrivateKey pk = decodePrivateKey(pkEntry.privateKey, keyFactory);
            X509Certificate[] certChain = new X509Certificate[pkEntry.certChain.length];
            int i = 0;
            for (byte[] certBytes : pkEntry.certChain) {
                certChain[i++] = decodeCertificate(certBytes);
            }
            privateKeyEntries.add(
                new PrivateKeyEntry(pkEntry.alias, pk, pkEntry.password, certChain));
        }

        return privateKeyEntries;
    }

    public Collection<KeyFileEntry> getKeyFiles() {
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("RSA", BC_PROVIDER);
        } catch (NoSuchAlgorithmException e) {
            throw ErrorUtils
                .logExceptionAndWrapFmt(log, e, "Failed to create key factory. Exception: {0}");
        }

        Collection<KeyFileEntry> keyFileEntries
            = new ArrayList<>(data.keyFiles.length);
        for (DataKeyFileEntry pkEntry : data.keyFiles) {
            Key key;
            if (pkEntry.privateKey) {
                key = decodePrivateKey(pkEntry.key, keyFactory);
            } else {
                key = decodePublicKey(pkEntry.key, keyFactory);
            }
            keyFileEntries
                .add(new KeyFileEntry(pkEntry.format, key, pkEntry.privateKey, pkEntry.filePath));
        }

        return keyFileEntries;
    }

    @NotNull
    @Override
    public Class<? extends IAutomationFlow> autowiredFlow() {
        return SslCertsAndKeysFlow.class;
    }

    @Override
    public Map<String, String> serialize(String key) {
        return envPropSerializer.serialize(key);
    }

    @Override
    public SslCertsAndKeysFlowContext deserialize(String key, Map<String, String> serializedData) {
        return envPropSerializer.deserialize(key, serializedData);
    }

    public static class DataCertEntry {
        String alias;
        byte[] cert;

        public DataCertEntry(String alias, byte[] cert) {
            this.alias = alias;
            this.cert = cert;
        }
    }

    public static class DataPrivateKeyEntry {
        String alias;
        String password;
        byte[] privateKey;
        byte[][] certChain;

        public DataPrivateKeyEntry(String alias, String password, byte[] privateKey,
            byte[][] certChain) {
            this.alias = alias;
            this.password = password;
            this.privateKey = privateKey;
            this.certChain = certChain;
        }
    }

    public static class DataKeyFileEntry {
        KeyFileFormat format;
        byte[] key;
        boolean privateKey;
        String filePath;

        public DataKeyFileEntry(KeyFileFormat format, byte[] key, boolean privateKey,
            String filePath) {
            this.format = format;
            this.key = key;
            this.privateKey = privateKey;
            this.filePath = filePath;
        }
    }

    public static class DataCertFileEntry {
        byte[] certificate;
        String filePath;

        public DataCertFileEntry(byte[] certificate, String filePath) {
            this.certificate = certificate;
            this.filePath = filePath;
        }
    }

    public static class Data {
        String keyStorePath;
        String keyStorePassword;
        DataCertEntry[] certs;
        DataPrivateKeyEntry[] privateKeys;
        DataCertFileEntry[] certFileEntries;
        DataKeyFileEntry[] keyFiles;
    }

    public static class Serializer extends
        AbstractEnvPropertySerializer<SslCertsAndKeysFlowContext> {
        private static final Logger log = LoggerFactory
            .getLogger(SslCertsAndKeysFlowContext.Serializer.class);
        private SslCertsAndKeysFlowContext flowContext;
        private Gson gson = new GsonBuilder().create();

        public Serializer(SslCertsAndKeysFlowContext flowContext) {
            super(SslCertsAndKeysFlowContext.Serializer.class);
            this.flowContext = flowContext;
        }

        @Override
        public SslCertsAndKeysFlowContext deserialize(String key,
            Map<String, String> serializedData) {
            log.debug("Serialized data: {}", serializedData);
            Map<String, String> deserializedMap = deserializeMapWithKey(key, serializedData);
            log.debug("Deserialized data: {}", deserializedMap);
            String jsonStr = deserializedMap.get(DATA);
            if (StringUtils.isBlank(jsonStr)) {
                throw new IllegalArgumentException("Empty deserialized data");
            }

            SslCertsAndKeysFlowContext.Data
                data = gson.fromJson(jsonStr, SslCertsAndKeysFlowContext.Data.class);
            if (data == null) {
                throw new IllegalArgumentException("JSON deserialization failure");
            }

            return new SslCertsAndKeysFlowContext(data);
        }

        @Override
        public Map<String, String> serialize(String key) {
            Args.notNull(flowContext, "Flow context");

            Map<String, String> serializeMap = super.serialize(key);
            serializeMap.putAll(
                serializeMapWithKey(key,
                    Collections.singletonMap(DATA, gson.toJson(flowContext.data))));

            return serializeMap;
        }
    }

    public static class Builder extends BuilderBase<Builder, SslCertsAndKeysFlowContext> {
        private static final Logger log = LoggerFactory.getLogger(Builder.class);

        protected String keystorePath;
        protected String keyStorePassword = "changeit";

        protected Collection<CertEntry> certs = new ArrayList<>(1);
        protected Collection<PrivateKeyEntry> privKeys = new ArrayList<>(1);
        protected Collection<CertFileEntry> certFiles = new ArrayList<>(1);
        protected Collection<KeyFileEntry> keyFiles = new ArrayList<>(1);

        protected DataCertEntry[] dataCertEntries = null;
        protected DataPrivateKeyEntry[] dataPrivateKeyEntries = null;
        protected DataCertFileEntry[] dataCertFileEntries = null;
        protected DataKeyFileEntry[] dataPrivateKeyFileEntries = null;

        protected static byte[] getEncodedCert(X509Certificate certificate) {
            try {
                return certificate.getEncoded();
            } catch (CertificateEncodingException e) {
                throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                    "Certificate encoding failed. Exception: {0}");
            }
        }

        public Builder setKeyStorePath(String keyStorePath) {
            this.keystorePath = keyStorePath;
            return builder();
        }

        public Builder setKeyStorePassword(String keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
            return builder();
        }

        public Builder addCertToKeystore(String alias, X509Certificate certificate) {
            this.certs.add(new CertEntry(alias, certificate));
            return builder();
        }

        public Builder writeCertToPemFile(String filePath, X509Certificate certificate) {
            this.certFiles.add(new CertFileEntry(certificate, filePath));
            return builder();
        }

        public Builder addPrivateKeyToKeystore(String alias, PrivateKey privateKey, String password,
            List<X509Certificate> certChain) {
            this.privKeys.add(new PrivateKeyEntry(alias, privateKey, password,
                certChain.toArray(new X509Certificate[certChain.size()])));
            return builder();
        }

        public Builder writeKeyToPemFile(KeyFileFormat format, String filePath,
            Key key, boolean privateKey) {
            this.keyFiles.add(new KeyFileEntry(format, key, privateKey, filePath));
            return builder();
        }

        @Override
        protected SslCertsAndKeysFlowContext getInstance() {
            return new SslCertsAndKeysFlowContext(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public SslCertsAndKeysFlowContext build() {
            dataCertEntries = new DataCertEntry[certs.size()];
            int i = 0;
            for (CertEntry certEntry : certs) {
                dataCertEntries[i++] = new DataCertEntry(certEntry.alias,
                    getEncodedCert(certEntry.certificate));
            }

            i = 0;
            dataPrivateKeyEntries = new DataPrivateKeyEntry[privKeys.size()];
            for (PrivateKeyEntry pkEntry : privKeys) {
                byte[][] certChainBytes = new byte[pkEntry.certChain.length][];
                int k = 0;
                for (X509Certificate cert : pkEntry.certChain) {
                    certChainBytes[k++] = getEncodedCert(cert);
                }
                dataPrivateKeyEntries[i++] = new DataPrivateKeyEntry(
                    pkEntry.alias, pkEntry.password, pkEntry.privateKey.getEncoded(),
                    certChainBytes);
            }

            i = 0;
            dataCertFileEntries = new DataCertFileEntry[certFiles.size()];
            for (CertFileEntry certFileEntry : certFiles) {
                dataCertFileEntries[i++] = new DataCertFileEntry(
                    getEncodedCert(certFileEntry.certificate), certFileEntry.filePath);
            }

            i = 0;
            dataPrivateKeyFileEntries = new DataKeyFileEntry[keyFiles.size()];
            for (KeyFileEntry keyEntry : keyFiles) {
                dataPrivateKeyFileEntries[i++] = new DataKeyFileEntry(keyEntry.format,
                    keyEntry.key.getEncoded(), keyEntry.privateKey, keyEntry.filePath);
            }

            return getInstance();
        }


    }

    public static class CertEntry {
        public String alias;
        public X509Certificate certificate;

        public CertEntry(String alias, X509Certificate certificate) {
            this.alias = alias;
            this.certificate = certificate;
        }
    }

    public static class PrivateKeyEntry {
        public String alias;
        public String password;
        public PrivateKey privateKey;
        public X509Certificate[] certChain;

        public PrivateKeyEntry(String alias, PrivateKey privateKey, String password,
            X509Certificate[] certChain) {
            this.alias = alias;
            this.privateKey = privateKey;
            this.password = password;
            this.certChain = certChain;
        }
    }

    public static class KeyFileEntry {
        public KeyFileFormat format;
        public Key key;
        public boolean privateKey;
        public String filePath;
        public KeyFileEntry(KeyFileFormat format, Key key, boolean privateKey, String filePath) {
            this.format = format;
            this.key = key;
            this.privateKey = privateKey;
            this.filePath = filePath;
        }

        public enum KeyFileFormat {
            PEM,
            DER
        }
    }

    public static class CertFileEntry {
        public X509Certificate certificate;
        public String filePath;

        public CertFileEntry(X509Certificate cert, String filePath) {
            this.certificate = cert;
            this.filePath = filePath;
        }
    }
}
