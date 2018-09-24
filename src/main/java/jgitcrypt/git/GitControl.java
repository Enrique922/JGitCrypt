package jgitcrypt.git;

import jgitcrypt.constt.Constants;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.concurrent.atomic.AtomicReference;

/*
*
*
* String localPath = "/home/user/repos/gittutorial";
  String remotePath = "https://github.com/GeekyTheory/GitTutorial.git";
  GitControl gc = new GitControl(localPath, remotePath);
  //Clone repository
  gc.cloneRepo();
  //Add files to repository
  gc.addToRepo();
  //Commit with a custom message
  gc.commitToRepo("Modified testfile.txt");
  //Push commits
  gc.pushForceToRepo();
  //Pull
  gc.pullFromRepo();

*
* */
@Slf4j
public class GitControl {
    static {
        //JavaSSLCertificate.disableSslVerification();
    }

    private String localPath;//por parametro
    private String remotePath;//por parametro
    private Git git;
    private CredentialsProvider cp;
    private StoredConfig config;
    private GitExec gitcmd;
    private boolean userGitExec = false;

    private void JGitSelfInstance() throws GitAPIException {
        git = Git.init().setDirectory(new File(localPath)).call();
    }

    private void GitExecInstance(jgitcrypt.type.Git param_git, String localPath, String remotePath) {
        gitcmd = new GitExec(param_git.getExec());
        gitcmd.setDebug(param_git.isDebug());
        gitcmd.setAutoPush(param_git.isAutopush());
        gitcmd.createGitIgnore(localPath);
        gitcmd.init();
        if (remotePath != null) {
            gitcmd.addRemote(remotePath);
        }
        userGitExec = true;
    }

    public GitControl(String localPath, jgitcrypt.type.Git param_git/*,String user, String password, String gitExec, boolean debug*/) throws IOException, GitAPIException {
        //Proxy
        setProxy("", "");

        this.localPath = localPath;
        this.remotePath = param_git.getUrl();

        //////////////////////
        if (param_git.getExec() != null) {
            //////////////////////
            GitExecInstance(param_git, localPath, remotePath);
            //////////////////////
        } else {
            //////////////////////
            JGitSelfInstance();
            //////////////////////
            FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
            Repository repository = repositoryBuilder.setGitDir(new File(localPath + "/.git"))
                    .readEnvironment() // scan environment GIT_* variables
                    .findGitDir() // scan up the file system tree
                    .setMustExist(true)
                    .build();
            cp = new UsernamePasswordCredentialsProvider(param_git.getCredentials().getUser(), param_git.getCredentials().getPassword());
            //////////////////////
            git = new Git(repository);
            //////////////////////
            config = git.getRepository().getConfig();
            configProperties();
            config.save();
            //////////////////////
        }

    }
    public void close(){
        if(git!=null)
            git.close();
    }

    private void validateAndSetConfig(String sec, String subsec, String name, String value) {
        if (config.getString(sec, subsec, name) == null) {
            config.setString(sec, subsec, name, value);
        }
    }

    private void configProperties() {
        validateAndSetConfig(Constants.REMOTE, Constants.ORIGIN, "url", remotePath);
        validateAndSetConfig(Constants.REMOTE, Constants.ORIGIN, "fetch", "+refs/heads/*:refs/remotes/origin/*");
        validateAndSetConfig(Constants.REMOTE, Constants.MASTER, Constants.REMOTE, Constants.ORIGIN);
        validateAndSetConfig(Constants.REMOTE, Constants.MASTER, "merge", "refs/heads/master");
        validateAndSetConfig(Constants.REMOTE, Constants.ORIGIN, "sslverify", "false");
        validateAndSetConfig(Constants.REMOTE, Constants.ORIGIN, "proxy", "http://f3xcl1r:Papa145*@proxy2:8008");
        //validateAndSetConfig(Constants.REMOTE, Constants.ORIGIN, "proxy", "https://f3xcl1r:Diego145*@proxy2:8008");
    }

    private void setProxy(String usuario, String clave) {
        final String authUser = "f3xcl1r";
        final AtomicReference<String> authPassword = new AtomicReference<>("Papa145*");
        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                authUser, authPassword.get().toCharArray());
                    }
                }
        );
        System.setProperty("http.proxyUser", authUser);
        System.setProperty("http.proxyPassword", authPassword.get());
    }


    public void cloneRepo() throws GitAPIException {
        if (userGitExec) {
            gitcmd.cloneRepo(remotePath);
        } else {
            Git.cloneRepository()
                    .setURI(remotePath)
                    .setDirectory(new File(localPath))
                    .call();
        }
    }

    public void addToRepo() throws GitAPIException {
        if (userGitExec) {
            gitcmd.addToRepo(".");
        } else {
            AddCommand add = git.add();
            add.addFilepattern(".").call();
        }
    }

    public void commitToRepo(String message) throws GitAPIException {
        if (userGitExec) {
            gitcmd.commitToRepo(message);
        } else {
            git.commit().setMessage(message).call();
        }
    }

    public void pushForceToRepo() throws GitAPIException {
        if (userGitExec) {
            gitcmd.pushForceToRepo();
        } else {
            PushCommand pc = git.push().setCredentialsProvider(cp);
            Iterable<PushResult> results = pc.setCredentialsProvider(cp)
                    .setRemote(this.remotePath)
                    .setForce(true)
                    .setPushAll().call();

            int updates = 0;
            for (PushResult result : results) {
                updates += result.getRemoteUpdates().size();
            }
            if (updates == 0) {
                log.error("No updates pushed. Something maybe failed?");
            } else if (updates == 1) {
                log.info("Update pushed.");
            } else {
                log.info(updates + " updates pushed.");
            }
        }
    }

    public void pullFromRepo() throws GitAPIException {
        if (userGitExec) {
            gitcmd.pullFromRepo();
        } else {
            git.pull().call();
        }
    }

    public void fetchFromRepo() {
        if (userGitExec) {
            gitcmd.fetch();
        }
    }
}
