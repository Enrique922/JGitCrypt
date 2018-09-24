package jgitcrypt.util;

import jgitcrypt.constt.Constants;
import jgitcrypt.type.CreateKey;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.*;
import java.util.Date;

public class RSAKeyPairGenerator {
    private RSAKeyPairGenerator() {
    }

    private static void exportKeyPair(OutputStream paramOutputStream1,
                                      OutputStream paramOutputStream2,
                                      String id,
                                      char[] password,
                                      boolean armor,KeyPairGenerator paramKeyPairGenerator) throws IOException, PGPException {
        if (armor) {
            paramOutputStream1 = new ArmoredOutputStream(paramOutputStream1);
        }
        PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder().build().get(HashAlgorithmTags.SHA1);
        PGPKeyPair keyPair = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, paramKeyPairGenerator.generateKeyPair(), new Date());
        PGPSecretKey localPGPSecretKey = new PGPSecretKey(PGPSignature.DEFAULT_CERTIFICATION, keyPair,
                id, sha1Calc, null,
                null, new JcaPGPContentSignerBuilder(keyPair.getPublicKey().getAlgorithm(),
                HashAlgorithmTags.SHA1),
                new JcePBESecretKeyEncryptorBuilder(PGPEncryptedData.CAST5, sha1Calc).setProvider("BC")
                        .build(password));
        localPGPSecretKey.encode(paramOutputStream1);
        paramOutputStream1.close();
        if (armor) {
            paramOutputStream2 = new ArmoredOutputStream(paramOutputStream2);
        }
        PGPPublicKey localPGPPublicKey = localPGPSecretKey.getPublicKey();
        localPGPPublicKey.encode(paramOutputStream2);
        paramOutputStream2.close();
    }

    public static String generateKeys(CreateKey createKey) throws NoSuchProviderException, NoSuchAlgorithmException, IOException, PGPException {
        KeyPairGenerator localKeyPairGenerator = KeyPairGenerator.getInstance(Constants.NAME_ALGORITHM, Constants.NAME_PROVIDER);
        localKeyPairGenerator.initialize(Constants.KEY_SIZE);
        exportKeyPair(
                new FileOutputStream(
                        createKey.getPathKeys() + Constants.NAME_PRIVATE_KEY),
                new FileOutputStream(
                        createKey.getPathKeys() + Constants.NAME_PUBLIC_KEY),
                createKey.getId(),
                createKey.getPassword().toCharArray(),
                false,//createKey.isArmor(),
                localKeyPairGenerator);
        return createKey.getPathKeys() + Constants.NAME_PUBLIC_KEY;
    }
/*
    public static void main(String[] paramArrayOfString) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator localKeyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        localKeyPairGenerator.initialize(2048);
        KeyPair localKeyPair = localKeyPairGenerator.generateKeyPair();

        FileOutputStream keyprivada;
        FileOutputStream keypublica;

        keyprivada = new FileOutputStream(
                "C:\\Users\\f3xcl1r\\IdeaProjects\\jgitcrypt\\src\\main\\java\\jgitcrypt\\keyprivada.asc");
        keypublica = new FileOutputStream(
                "C:\\Users\\f3xcl1r\\IdeaProjects\\jgitcrypt\\src\\main\\java\\jgitcrypt\\keypublica.asc");
        exportKeyPair(keyprivada, keypublica,
                localKeyPair.getPublic(), localKeyPair.getPrivate(),
                "Daniel Navas Sánchez",
                "g9!w8!a8!".toCharArray(),
                true);

    }*/
}
