package jgitcrypt.git;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

@Slf4j
class GitExec {
    private boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
    private String pathExecGit;
    private boolean autoPush = true;

    GitExec(String pathExecGit) {
        this.pathExecGit = pathExecGit;
        credentialsPopUp();
    }

    void createGitIgnore(String path) {
        try {
            String fileData = "target/\n" +
                    "!.mvn/wrapper/maven-wrapper.jar\n" +
                    "\n" +
                    "### STS ###\n" +
                    ".apt_generated\n" +
                    ".classpath\n" +
                    ".factorypath\n" +
                    ".project\n" +
                    ".settings\n" +
                    ".springBeans\n" +
                    "\n" +
                    "### IntelliJ IDEA ###\n" +
                    ".idea\n" +
                    "*.iws\n" +
                    "*.iml\n" +
                    "*.ipr\n" +
                    "\n" +
                    "### NetBeans ###\n" +
                    "nbproject/private/\n" +
                    "build/\n" +
                    "nbbuild/\n" +
                    "dist/\n" +
                    "nbdist/\n" +
                    ".nb-gradle/\n" +
                    "\n" +
                    "#JGitCrypt Daniel Navas Sanchez\n" +
                    "*.java\n" +
                    "*.asc";
            Files.write(Paths.get(path + "/.gitignore"), fileData.getBytes());
            Thread.sleep(1000);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    void exec(String command,boolean global) {
        try {
            String[] var_command = new String[3];
            if (isWindows) {
                var_command[0] = "cmd";
                var_command[1] = "/C";
                if(global){
                    var_command[2] = command;
                }else{
                    var_command[2] = this.pathExecGit + " " +command;
                }
            }else{
                var_command[0] = "sh";
                var_command[1] = "-c";
                if(global){
                    var_command[2] = command;
                }else{
                    var_command[2] = this.pathExecGit + " " +command;
                }
            }
            log.info("Command:" + Arrays.toString(var_command));
            //////////////////
            Process p = new ProcessBuilder().command(var_command).inheritIO().start();

            BufferedInputStream bis = new BufferedInputStream(p.getInputStream());
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            int result = bis.read();
            while(result != -1) {
                buf.write((byte) result);
                result = bis.read();
            }
            bis.close();
            log.info("Response: "+buf.toString("UTF-8"));
            try {
                p.waitFor();
            }catch (InterruptedException e) {
                log.error("process was interrupted");
            }
            if (p.exitValue() != 0)
                log.info("JGitCrypt Exit was non-zero %d %n",p.exitValue());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void credentialsPopUp() {
        exec(" config credential.helper manager",false);
    }

    void init() {
        exec(" init ",false);
    }

    void addToRepo(String s) {
        exec(" add " + s,false);
    }

    void commitToRepo(String message) {
        exec(" commit -m  \"" + message + "\"",false);
    }

    //push --progress --porcelain origin refs/heads/master:master
    void pushForceToRepo() {
        if(this.autoPush)
            exec(" push --force origin master",false);
    }

    void addRemote(String git) {
        exec(" remote add origin " + git,false);
    }

    void pullFromRepo() {
        exec(" pull origin master --allow-unrelated-histories ",false);
    }

    void fetch() {
        exec(" fetch origin --progress --prune",false);
    }

    void cloneRepo(String url) {
        exec(" clone "+url,false);
    }

    public void setDebug(boolean debug) {
        exec("set GIT_TRACE="+((debug)?"1":"0"), true);
        exec("set GCM_TRACE="+((debug)?"1":"0"), true);
    }

    public void setAutoPush(boolean autoPush) {
        this.autoPush = autoPush;
    }
}
