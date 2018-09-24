package jgitcrypt.util;

import jgitcrypt.constt.Constants;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;

import java.io.*;
import java.util.Iterator;

public class PGPJUtil {
    /////////////////////////////
    public static byte[] compressFile(String paramString, int paramInt) throws IOException {
        ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
        PGPCompressedDataGenerator localPGPCompressedDataGenerator = new PGPCompressedDataGenerator(paramInt);
        PGPUtil.writeFileToLiteralData(localPGPCompressedDataGenerator.open(localByteArrayOutputStream), 'b', new File(paramString));
        localPGPCompressedDataGenerator.close();
        return localByteArrayOutputStream.toByteArray();
    }

    /////////////////////////////
    /*public static PGPPrivateKey findSecretKey(PGPSecretKeyRingCollection paramPGPSecretKeyRingCollection, long paramLong, char[] paramArrayOfChar)
            throws PGPException {
        PGPSecretKey localPGPSecretKey = paramPGPSecretKeyRingCollection.getSecretKey(paramLong);
        if (localPGPSecretKey == null) {
            return null;
        }
        return localPGPSecretKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder(
                new JcaPGPDigestCalculatorProviderBuilder().setProvider("BC").build())
                .setProvider("BC").build(paramArrayOfChar));
    }*/

    public static PGPPrivateKey findSecretKey(PGPSecretKeyRingCollection pgpSec, long keyID, char[] pass) throws PGPException {
        PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);
        if (pgpSecKey == null) {
            return null;
        }
        return pgpSecKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(pass));
    }

    /////////////////////////////
    public static PGPPublicKey readPublicKey(String paramString) throws IOException, PGPException {
        BufferedInputStream localBufferedInputStream = new BufferedInputStream(new FileInputStream(paramString));
        PGPPublicKey localPGPPublicKey;
        try {
            localPGPPublicKey = readPublicKey(localBufferedInputStream);
        } finally {
            localBufferedInputStream.close();
        }
        return localPGPPublicKey;
    }

    /////////////////////////////
    private static PGPPublicKey readPublicKey(InputStream paramInputStream) throws IOException, PGPException {
        PGPPublicKeyRingCollection localPGPPublicKeyRingCollection =
                new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(paramInputStream), new JcaKeyFingerprintCalculator());
        Iterator localIterator1 = localPGPPublicKeyRingCollection.getKeyRings();
        while (localIterator1.hasNext()) {
            PGPPublicKeyRing localPGPPublicKeyRing = (PGPPublicKeyRing) localIterator1.next();
            Iterator localIterator2 = localPGPPublicKeyRing.getPublicKeys();
            while (localIterator2.hasNext()) {
                PGPPublicKey localPGPPublicKey = (PGPPublicKey) localIterator2.next();
                if (localPGPPublicKey.isEncryptionKey()) {
                    return localPGPPublicKey;
                }
            }
        }
        throw new IllegalArgumentException("Can't find encryption key in key ring.");
    }

    /////////////////////////////
    public static PGPSecretKey readSecretKey(String paramString) throws IOException, PGPException {
        BufferedInputStream localBufferedInputStream = new BufferedInputStream(new FileInputStream(paramString));
        PGPSecretKey localPGPSecretKey = readSecretKey(localBufferedInputStream);
        localBufferedInputStream.close();
        return localPGPSecretKey;
    }

    /////////////////////////////
    static PGPSecretKey readSecretKey(InputStream paramInputStream) throws IOException, PGPException {
        PGPSecretKeyRingCollection localPGPSecretKeyRingCollection =
                new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(paramInputStream), new JcaKeyFingerprintCalculator());
        Iterator localIterator1 = localPGPSecretKeyRingCollection.getKeyRings();
        while (localIterator1.hasNext()) {
            PGPSecretKeyRing localPGPSecretKeyRing = (PGPSecretKeyRing) localIterator1.next();
            Iterator localIterator2 = localPGPSecretKeyRing.getSecretKeys();
            while (localIterator2.hasNext()) {
                PGPSecretKey localPGPSecretKey = (PGPSecretKey) localIterator2.next();
                if (localPGPSecretKey.isSigningKey()) {
                    return localPGPSecretKey;
                }
            }
        }
        throw new IllegalArgumentException("Can't find signing key in key ring.");
    }

    public static byte[] getBytesFromFile(File f) throws IOException {
        if (f == null) {
            return null;
        }
        ByteArrayOutputStream out;
        try (FileInputStream stream = new FileInputStream(f)) {
            out = new ByteArrayOutputStream(1024);
            byte[] b = new byte[1024];
            int n;
            while ((n = stream.read(b)) != -1) {
                out.write(b, 0, n);
            }
        }
        out.close();
        return out.toByteArray();
    }
}
