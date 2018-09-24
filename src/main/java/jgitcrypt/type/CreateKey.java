package jgitcrypt.type;

import java.io.File;

public class CreateKey {
    /**
     * ID.
     * Identidad.
     * @parameter
     */
    private String id;

    /**
     * Password.
     * Clave.
     * @parameter
     */
    private String password;
    /**
     * Folder keys PGP.
     * Ruta donde generar los Claves PGP.
     * @parameter expression="${project.build.sourceDirectory}"
     */
    private String pathKeys;

    /**
     * Encode the input data producing a base 64 encoded byte array.
     * Codificar los archivos en base 64 y con cabecera de firma BCPG;
     * -----BEGIN PGP MESSAGE-----
     *  Version: BCPG v1.46.
     *  XXX
     *-----END PGP MESSAGE-----
     * @parameter
     */
    private boolean armor;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPathKeys() {
        return pathKeys.endsWith(File.separator)? pathKeys : pathKeys.concat(File.separator);
    }

    public void setPathKeys(String pathKeys) {
        this.pathKeys = pathKeys;
    }

    public boolean isArmor() {
        return armor;
    }

    public void setArmor(boolean armor) {
        this.armor = armor;
    }
}
