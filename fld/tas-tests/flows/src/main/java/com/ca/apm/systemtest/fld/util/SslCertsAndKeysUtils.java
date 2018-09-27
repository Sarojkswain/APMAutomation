package com.ca.apm.systemtest.fld.util;

import com.ca.apm.systemtest.fld.common.ErrorUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNamesBuilder;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.X509KeyUsage;
import org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentVerifierProviderBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.Encodable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;
import java.util.UUID;

/**
 * @author haiva01
 */
public final class SslCertsAndKeysUtils {
    public static final String CA_CERT_X500NAME_STR
        = "C=US, O=CA Technologies, OU=APM, OU=Aquarius, CN=Faux CA";
    public static final X500Name CA_CERT_X500NAME = new X500Name(CA_CERT_X500NAME_STR);
    public static final String CERT_SIGNING_ALGO = "SHA512WithRSA";
    public static final Provider BC_PROVIDER = new BouncyCastleProvider();
    private static final Logger log = LoggerFactory.getLogger(SslCertsAndKeysUtils.class);

    public static BigInteger getBigIntegerFromUuid(UUID randomUUID) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(randomUUID.getMostSignificantBits());
        bb.putLong(randomUUID.getLeastSignificantBits());
        return new BigInteger(bb.array());
    }

    public static KeyPair genRsaKeyPair() {
        try {
            KeyPairGenerator rsa = KeyPairGenerator.getInstance("RSA", BC_PROVIDER);
            rsa.initialize(4096);
            KeyPair kp = rsa.generateKeyPair();
            return kp;
        } catch (Exception e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to generate RSA key pair. Exception: {0}");
        }
    }

    public static void writeBytesToFile(byte[] bytes, File file) throws IOException {
        FileUtils.forceMkdirParent(file);
        file.delete();
        Files.write(file.toPath(), bytes, StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.CREATE);
    }

    public static void writeEncodable(File dest, String name,
        Encodable encodable) throws IOException {
        byte[] encodedCert = encodable.getEncoded();
        File caCertFile = dest.getAbsoluteFile();
        log.info("Writing {} into {}", name, caCertFile.getAbsolutePath());
        writeBytesToFile(encodedCert, caCertFile);
    }

    public static void writeX509CertificateDer(File dest, String name, X509Certificate cert)
        throws IOException, CertificateEncodingException {
        byte[] encodedCert = cert.getEncoded();
        File caCertFile = dest.getAbsoluteFile();
        log.info("Writing {} into {}", name, caCertFile.getAbsolutePath());
        writeBytesToFile(encodedCert, caCertFile);
    }

    public static void writeKeyDer(File dest, String name, Key key) throws IOException {
        byte[] encodedKey = key.getEncoded();
        File keyFile = dest.getAbsoluteFile();
        log.info("Writing {} into {}", name, keyFile.getAbsolutePath());
        writeBytesToFile(encodedKey, keyFile);
    }

    public static void writeKeyPair(File dest, String name, KeyPair keyPair) throws IOException {
        File privateKeyFile = new File(dest.getAbsoluteFile().getParentFile(),
            dest.getName() + ".private");
        writeKeyDer(privateKeyFile, name + " private key", keyPair.getPrivate());

        File publicKeyFile = new File(dest.getAbsoluteFile().getParentFile(),
            dest.getName() + ".public");
        writeKeyDer(publicKeyFile, name + " public key", keyPair.getPublic());
    }

    public static void writeX509CertificatePem(File dest, String name, X509Certificate cert)
        throws IOException, CertificateEncodingException {
        File certFile = dest.getAbsoluteFile();
        log.info("Writing {} into {}", name, certFile.getAbsolutePath());
        FileUtils.forceMkdirParent(certFile);
        certFile.delete();
        try (Writer fileWriter = new FileWriterWithEncoding(certFile, StandardCharsets.US_ASCII);
             JcaPEMWriter pemWriter = new JcaPEMWriter(fileWriter)) {
            pemWriter.writeObject(cert);
        }
    }

    public static void writeKeyPem(File dest, String name, Key key) throws IOException,
        CertificateEncodingException {
        File keyFile = dest.getAbsoluteFile();
        log.info("Writing {} into {}", name, keyFile.getAbsolutePath());
        FileUtils.forceMkdirParent(keyFile);
        keyFile.delete();
        try (Writer fileWriter = new FileWriterWithEncoding(keyFile, StandardCharsets.US_ASCII);
             JcaPEMWriter pemWriter = new JcaPEMWriter(fileWriter)) {
            pemWriter.writeObject(key);
        }
    }

    public static void verifyCertificate(
        X509CertificateHolder certHolder) throws OperatorCreationException, CertificateException,
        CertException {
        ContentVerifierProvider contentVerifierProvider
            = new BcRSAContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder())
            .build(certHolder);
        log.info("checking just generated certificate validity: {}",
            certHolder.isSignatureValid(contentVerifierProvider));
    }

    public static CertInfo createCert(PrivateKey caPrivKey, PublicKey caPubKey, String hostName) {
        log.info("Generating signed certificate for \"{}\"", hostName);
        try {
            // Generate key pair for new certificate.
            final KeyPair kp = genRsaKeyPair();

            // subjects name table.
            X500NameBuilder subjectBuilder = new X500NameBuilder();
            subjectBuilder.addRDN(BCStyle.C, "US");
            subjectBuilder.addRDN(BCStyle.O, "CA Technologies");
            subjectBuilder.addRDN(BCStyle.OU, "APM");
            subjectBuilder.addRDN(BCStyle.OU, "Aquarius");
            subjectBuilder.addRDN(BCStyle.CN, hostName);
            subjectBuilder.addRDN(BCStyle.EmailAddress, "Team-APM-Aquarius@ca.com");

            // create the certificate - version 3
            Date now = new Date();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, 1);
            Date endDate = cal.getTime();
            X509v3CertificateBuilder v3Bldr = new JcaX509v3CertificateBuilder(CA_CERT_X500NAME,
                getBigIntegerFromUuid(UUID.randomUUID()), now, endDate, subjectBuilder.build(),
                kp.getPublic());

            // extensions
            JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();

            final SubjectKeyIdentifier subjectKeyIdentifier
                = extUtils.createSubjectKeyIdentifier(kp.getPublic());
            v3Bldr.addExtension(Extension.subjectKeyIdentifier, false, subjectKeyIdentifier);

            GeneralNamesBuilder sansBuilder = new GeneralNamesBuilder();
            Collection<String> altHostNames = new TreeSet<>();
            altHostNames.add(hostName);
            altHostNames.add(hostName.split("\\.")[0]);
            for (String name : altHostNames) {
                sansBuilder.addName(new GeneralName(GeneralName.dNSName, name));
            }
            Extension sanExtension = new Extension(Extension.subjectAlternativeName, false,
                new DEROctetString(sansBuilder.build()));
            v3Bldr.addExtension(sanExtension);

            v3Bldr.addExtension(Extension.authorityKeyIdentifier, false,
                extUtils.createAuthorityKeyIdentifier(caPubKey));

            v3Bldr.addExtension(Extension.keyUsage, true,
                new X509KeyUsage(X509KeyUsage.digitalSignature
                    | X509KeyUsage.keyEncipherment
                    | X509KeyUsage.dataEncipherment
                    | X509KeyUsage.keyAgreement));

            v3Bldr.addExtension(Extension.basicConstraints, false, new BasicConstraints(false));

            X509CertificateHolder certHldr = v3Bldr.build(
                new JcaContentSignerBuilder(CERT_SIGNING_ALGO)
                    .setProvider(BC_PROVIDER)
                    .build(caPrivKey));

            X509Certificate cert = new JcaX509CertificateConverter().setProvider(BC_PROVIDER)
                .getCertificate(certHldr);

            cert.checkValidity(new Date());
            cert.verify(caPubKey);

            PKCS12BagAttributeCarrier bagAttr = (PKCS12BagAttributeCarrier) cert;

            // this is also optional - in the sense that if you leave this out the keystore will add
            // it automatically, note though that for the browser to recognise the associated
            // private

            // key this you should at least use the pkcs_9_localKeyId OID and set it to the same as
            // you do for the private key's localKeyId.
            bagAttr.setBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName,
                new DERBMPString(hostName + "'s cert"));
            bagAttr.setBagAttribute(
                PKCSObjectIdentifiers.pkcs_9_at_localKeyId,
                subjectKeyIdentifier);

            bagAttr = (PKCS12BagAttributeCarrier) kp.getPrivate();
            bagAttr.setBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName,
                new DERBMPString(hostName + "'s private key"));
            bagAttr.setBagAttribute(
                PKCSObjectIdentifiers.pkcs_9_at_localKeyId,
                subjectKeyIdentifier);

            return new CertInfo(kp, cert);
        } catch (Exception e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to create certificate and/or keys. Exception: {0}");
        }
    }

    public static CertInfo genCaCert() {
        log.info("Generating CA certificate");
        try {
            final KeyPair kp = genRsaKeyPair();

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, 1);

            byte[] pk = kp.getPublic().getEncoded();
            //writeKeyPair(new File("faux-ca"), "Faux CA", kp);
            SubjectPublicKeyInfo bcPk = SubjectPublicKeyInfo.getInstance(pk);

            JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
            X509v3CertificateBuilder certGen = new X509v3CertificateBuilder(
                CA_CERT_X500NAME, getBigIntegerFromUuid(UUID.randomUUID()), new Date(),
                cal.getTime(),
                CA_CERT_X500NAME, bcPk)
                .addExtension(Extension.keyUsage, true,
                    new X509KeyUsage(X509KeyUsage.digitalSignature
                        | X509KeyUsage.keyCertSign))
                .addExtension(Extension.basicConstraints, false, new BasicConstraints(true))
                .addExtension(Extension.subjectKeyIdentifier, true,
                    extUtils.createSubjectKeyIdentifier(kp.getPublic()));

            X509CertificateHolder certHolder = certGen.build(
                new JcaContentSignerBuilder(CERT_SIGNING_ALGO)
                    .setProvider(BC_PROVIDER)
                    .build(kp.getPrivate()));

            verifyCertificate(certHolder);

            X509Certificate cert = new JcaX509CertificateConverter().setProvider(BC_PROVIDER)
                .getCertificate(certHolder);

            PKCS12BagAttributeCarrier bagAttr = (PKCS12BagAttributeCarrier) cert;
            bagAttr.setBagAttribute(
                PKCSObjectIdentifiers.pkcs_9_at_friendlyName,
                new DERBMPString("Faux CA certificate for APM deployment and testing"));

            return new CertInfo(kp, cert);
        } catch (Exception e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to generate CA certificate and/or keys. Exception: {0}");
        }
    }

    public static byte[] getEncodedCert(X509Certificate certificate) {
        try {
            return certificate.getEncoded();
        } catch (CertificateEncodingException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Certificate encoding failed. Exception: {0}");
        }
    }

    public static X509Certificate decodeCertificate(byte[] bytes) {
        X509Certificate cert;
        try {
            X509CertificateHolder certificateHolder = new X509CertificateHolder(bytes);
            cert = new JcaX509CertificateConverter().setProvider(BC_PROVIDER)
                .getCertificate(certificateHolder);
        } catch (CertificateException | IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to decode certificate from bytes. Exception: {0}");
        }
        return cert;
    }

    public static PrivateKey decodePrivateKey(byte[] bytes, KeyFactory keyFactory) {
        try {
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } catch (InvalidKeySpecException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to decode private key from bytes. Exception: {0}");
        }
    }

    public static PublicKey decodePublicKey(byte[] bytes, KeyFactory keyFactory) {
        try {
            return keyFactory.generatePublic(new X509EncodedKeySpec(bytes));
        } catch (InvalidKeySpecException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to decode public key from bytes. Exception: {0}");
        }
    }

    public static String getCanonicalHostName(String host) {
        try {
            InetAddress momHostAddr = InetAddress.getByName(host);
            return momHostAddr.getCanonicalHostName();
        } catch (Throwable e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to get canonical host name for {1}. Exception: {0}", host);
        }
    }

    public static class CertInfo {
        public KeyPair kp;
        public X509Certificate certificate;

        public CertInfo(KeyPair kp, X509Certificate certificate) {
            this.kp = kp;
            this.certificate = certificate;
        }
    }
}
