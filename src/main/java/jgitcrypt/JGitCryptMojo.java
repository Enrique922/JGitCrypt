package jgitcrypt;

import jgitcrypt.constt.Constants;
import jgitcrypt.git.GitControl;
import jgitcrypt.type.CreateKey;
import jgitcrypt.util.PGPJUtil;
import jgitcrypt.util.RSAKeyPairGenerator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.util.io.Streams;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

/**
 * Goal which JGitCrypt
 *
 * @goal jgitcrypt
 * @phase process-sources
 */
public class JGitCryptMojo extends AbstractMojo {
    /**
     * Folder SRC.
     * Directorio SRC del Proyecto (No es obligatorio)
     *
     * @parameter expression="${project.basedir}"
     * @required
     */
    private File outputDirectory;

    /**
     * Public key.
     * Clave Publica PGP (Si tiene)
     *
     * @parameter
     */
    private String keyPublicFile;

    /**
     * Private key.
     * Clave Privada PGP (Si tiene)
     *
     * @parameter
     */
    private String keyPrivateFile;

    /**
     * Clave para poder desencriptar los archivos bpg
     *
     * @parameter
     */
    private String password;

    /**
     * Decrypt los archivos bpg del Git y crear los .java
     *
     * @parameter
     */
    private boolean decrypt;

    /**
     * Create private Key and public.
     * Crear Clave privada y publica para encriptar archivos (Identidad y Clave)
     *
     * @parameter
     */
    private CreateKey createKey;

    private static final String SIGNING = "Signing...";
    private static final String DECRYPTING = "Decrypting...";

    /**
     * Repo Git .
     *
     * @parameter
     */
    private jgitcrypt.type.Git git;

    /**
     * Extensions Files to encrypt.
     *
     * @parameter
     */
    private String extensionsFiles;


    public void execute() throws MojoExecutionException {
        Security.addProvider(new BouncyCastleProvider());
        File f = new File(outputDirectory + "/src/main/java");
        if (decrypt) {
            if (keyPrivateFile != null && Files.exists(new File(keyPrivateFile).toPath())) {
                getLog().info(DECRYPTING);
                findAndDecryptFilesJava(f);
            }
        } else {
            if (keyPublicFile != null) {
                getLog().info(SIGNING);
                findAndSignFilesJava(f);
            } else if (createKey != null &&
                    (Files.notExists(new File(createKey.getPathKeys() + Constants.NAME_PUBLIC_KEY).toPath())
                            && Files.notExists(new File(createKey.getPathKeys() + Constants.NAME_PRIVATE_KEY).toPath()))
                    ) {
                getLog().info("Generating RSA Key Pair");
                try {
                    getLog().info(createKey.getPathKeys());
                    keyPublicFile = RSAKeyPairGenerator.generateKeys(createKey);
                    getLog().info(SIGNING);
                    findAndSignFilesJava(f);
                } catch (NoSuchProviderException | NoSuchAlgorithmException | IOException | PGPException e) {
                    getLog().error(e);
                }
            } else {
                getLog().info("RSA Key Pair Exists!!");
                getLog().info(SIGNING);
                keyPublicFile = createKey.getPathKeys() + Constants.NAME_PUBLIC_KEY;
                findAndSignFilesJava(f);
            }
        }
        if (!decrypt) {
            //JGitCrypt
            if (git == null) return;
            try {
                GitControl gitControl = new GitControl(outputDirectory.getAbsolutePath(), git);
                gitControl.addToRepo();
                gitControl.commitToRepo("Commit To Repo from JGitCrypt");
                /////////////////
                if (git.isAutopush()) gitControl.pushForceToRepo();
                gitControl.close();
            } catch (IOException | GitAPIException e) {
                getLog().error(e);
                throw new MojoExecutionException(e.getMessage());
            }
        }
    }

    private void findAndDecryptFilesJava(final File folder) {
        for (final File fileIn : folder.listFiles()) {
            if (fileIn.isDirectory()) {
                findAndDecryptFilesJava(fileIn);
            } else if (fileIn.getName().endsWith(".bpg")) {
                getLog().info("Decrypting File BPG to Java: " + fileIn.getName());
                try {
                    PGPEncrypt.decryptFileJavaBPG(password,
                            Streams.readAll(new FileInputStream(fileIn.getAbsolutePath())),
                            keyPrivateFile,
                            fileIn.getAbsolutePath().replace(Constants.EXTENSIONS_FILES,"")// + Constants.DECRYPT_EXTENSIONS_FILES,
                            );
                } catch (IOException | PGPException e) {
                    getLog().error(e);
                }
            }
        }
    }

    /////////////////////////
    private void findAndSignFilesJava(final File folder) {
        extensionsFiles = extensionsFiles == null ? ".java" : "."+extensionsFiles;
        for (final File fileIn : folder.listFiles()) {
            if (fileIn.isDirectory()) {
                findAndSignFilesJava(fileIn);
            } else if (fileIn.getName().endsWith(extensionsFiles/*".java"*/) || extensionsFiles.equals(".*")) {
                getLog().info("Signing File Java: " + fileIn.getName());
                try {
                    PGPEncrypt.signAndEncryptFile(
                            Streams.readAll(new FileInputStream(fileIn.getAbsolutePath())),
                            PGPJUtil.readPublicKey(keyPublicFile),
                            fileIn.getAbsolutePath() + Constants.EXTENSIONS_FILES);
                } catch (IOException | PGPException e) {
                    getLog().error(e);
                }
            }
        }
    }
}
