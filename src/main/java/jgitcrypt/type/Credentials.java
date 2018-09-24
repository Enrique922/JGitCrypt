package jgitcrypt.type;

public class Credentials {
    /**
     * Usuario del repositorio GitHub.
     *
     * @parameter
     */
    private String user;
    /**
     * Clave del repositorio GitHub
     *
     * @parameter
     */
    private String password;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
