package jgitcrypt;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;

import java.io.*;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Iterator;

public class PGPEncrypt {

    private PGPEncrypt() {
    }

    private static void decrypt(byte[] encrypted, InputStream keyIn, char[] password,
                                String fileName)
            throws IOException, PGPException {
        InputStream in = new ByteArrayInputStream(encrypted);

        in = PGPUtil.getDecoderStream(in);

        PGPObjectFactory pgpF = new PGPObjectFactory(in, new JcaKeyFingerprintCalculator());
        PGPEncryptedDataList enc;
        Object o = pgpF.nextObject();
        //
        // the first object might be a PGP marker packet.
        //
        if (o instanceof PGPEncryptedDataList) {
            enc = (PGPEncryptedDataList) o;
        } else {
            enc = (PGPEncryptedDataList) pgpF.nextObject();
        }
        //
        // find the secret key
        Iterator it = enc.getEncryptedDataObjects();
        PGPPrivateKey sKey = null;
        PGPPublicKeyEncryptedData pbe = null;
        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(keyIn), new JcaKeyFingerprintCalculator());

        while (sKey == null && it.hasNext()) {
            pbe = (PGPPublicKeyEncryptedData) it.next();
            PBESecretKeyDecryptor decryptor = new JcePBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider())
                    .build(password);

            PGPSecretKeyRing secretKey = pgpSec.getSecretKeyRing(pbe.getKeyID());
            PGPSecretKey key = secretKey.getSecretKey();
            sKey = key.extractPrivateKey(decryptor);
        }

        if (sKey == null) {
            throw new IllegalArgumentException("secret key for message not found.");
        }
        JcaPGPObjectFactory pgpFact;

        InputStream clear = pbe.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider("BC").build(sKey));
        pgpFact = new JcaPGPObjectFactory(clear);

        PGPCompressedData cData = (PGPCompressedData) pgpFact.nextObject();

        pgpFact = new JcaPGPObjectFactory(cData.getDataStream());

        PGPLiteralData ld = (PGPLiteralData) pgpFact.nextObject();

        InputStream unc = ld.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int ch;
        while ((ch = unc.read()) >= 0) {
            out.write(ch);
        }
        byte[] returnBytes = out.toByteArray();
        out.close();

        try (FileOutputStream dfis = new FileOutputStream(fileName)) {
            dfis.write(returnBytes);
        }
        in.close();
        keyIn.close();
    }

    static void decryptFileJavaBPG(String password, byte[] clearData, String pgpSecretKey, String fileName,
                                   boolean armor) throws IOException, PGPException {
        decrypt(clearData, new FileInputStream(pgpSecretKey), password.toCharArray(), fileName);
    }

    static void signAndEncryptFile(byte[] clearData,
                                   PGPPublicKey encKey,
                                   String fileName,
                                   boolean armor) throws IOException, PGPException {
        if (fileName == null) {
            fileName = PGPLiteralData.CONSOLE;
        }

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(fileName))) {
            /*if (armor) {
                out = new ArmoredOutputStream(out);
            }*/
            PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(PGPCompressedDataGenerator.UNCOMPRESSED);

            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            try (OutputStream cos = comData.open(bOut)) { // open it with the final destination

                PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();
                // we want to generate compressed data. This might be a user option  later,  in which case we would pass in bOut.
                try (
                        OutputStream pOut = lData.open(
                                cos, // the compressed output stream
                                PGPLiteralData.BINARY,
                                fileName, // "filename" to store
                                clearData.length, // length of clear data
                                new Date() // current time
                        )
                ) {
                    pOut.write(clearData);
                    PGPEncryptedDataGenerator cPk = new PGPEncryptedDataGenerator(
                            new BcPGPDataEncryptorBuilder(SymmetricKeyAlgorithmTags.CAST5)
                                    .setSecureRandom(new SecureRandom()));

                    cPk.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(encKey));

                    byte[] bytes = bOut.toByteArray();

                    try (OutputStream cOut = cPk.open(out, bytes.length)) {
                        cOut.write(bytes); // obtain the actual bytes from the compressed stream
                    }
                } finally {
                    lData.close();
                }
            }
        }
    }

    /*
    public static void main(String[] args) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        byte[] original = Streams.readAll(
                new FileInputStream("C:\\Users\\f3xcl1r\\IdeaProjects\\jgitcrypt\\src\\main\\java\\jgitcrypt\\testing.test")
        );
        System.out.println("Starting PGP signAndEncryptFile GIT");

        FileInputStream pubKey = new FileInputStream("C:\\Users\\f3xcl1r\\IdeaProjects\\jgitcrypt\\src\\main\\java\\jgitcrypt\\keypublica.asc");
        signAndEncryptFile(
                original,
                PGPJUtil.readPublicKey(pubKey),
                "C:\\Users\\f3xcl1r\\IdeaProjects\\jgitcrypt\\src\\main\\java\\jgitcrypt\\testing.test.bpg",
                true,
                true
        );

        FileOutputStream dfis = new FileOutputStream("C:\\Users\\f3xcl1r\\IdeaProjects\\jgitcrypt\\src\\main\\java\\jgitcrypt\\testing.test.bpg");
        dfis.write(encrypted);
        dfis.close();

        byte[] encFromFile = getBytesFromFile(new File("C:\\Users\\f3xcl1r\\IdeaProjects\\jgitcrypt\\src\\main\\java\\jgitcrypt\\testing.test.bpg"));
        FileInputStream secKey = new FileInputStream("C:\\Users\\f3xcl1r\\IdeaProjects\\jgitcrypt\\src\\main\\java\\jgitcrypt\\keyprivada.asc");

        byte[] decrypted = decrypt(encFromFile, secKey, "g9!w8!a8!".toCharArray());
    }
    */
}
