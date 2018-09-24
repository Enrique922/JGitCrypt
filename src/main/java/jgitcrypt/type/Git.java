package jgitcrypt.type;

public class Git {
    /**
     * Ejecutable Git
     * @parameter expression="git"
     */
    private String exec;

    /**
     * Debug ejecutable Git
     * @parameter
     */
    private boolean debug;

    /**
     * Url Repo Git .
     *
     * @parameter
     */
    private String url;

    /**
     * Auto push Git .
     *
     * @parameter
     */
    private boolean autopush = true;

    /**
     * Credenciales Repo Git .
     *
     * @parameter
     */
    private Credentials credentials;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public String getExec() {
        return exec;
    }

    public void setExec(String exec) {
        this.exec = exec;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isAutopush() {
        return autopush;
    }

    public void setAutopush(boolean autopush) {
        this.autopush = autopush;
    }
}
