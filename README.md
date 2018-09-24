# JGitCrypt

**`Run`**

`mvn com.daniel.navas:jgitcrypt:1.0.0:jgitcrypt`

_JGitCrypt is NOT NOT NOT NOT NOT designed to encrypt an entire repository._


**Example**

<plugin>
    
    <groupId>com.daniel.navas</groupId>
    <artifactId>jgitcrypt</artifactId>
    <version>1.0.0</version>
    <configuration> 
        
        <!-- Para Firmar-->
        <keyPublicFile>${project.build.sourceDirectory}/PublicKey.asc</keyPublicFile>
        <!-- Para desencriptar archivos bpg-->
        <keyPrivateFile>${project.build.sourceDirectory}/PrivateKey.asc</keyPrivateFile>
        <!-- Para desencriptar archivos bpg -->
        <password>passssssss</password>
        <!-- Parametro para desencriptar (true) los archivos bpg despues de clonar el repositorio
        Esto se realizara al primer "mvn clean install" 0 "mvn jgitcrypt:jgitcrypt" -->
        <!-- Despues de primer "mvn clean install" 0 "mvn jgitcrypt:jgitcrypt"
        se debe cambiar este parametro a false -->
        <!-- Si esta en false es para encriptar y subir los archivos al git-->
        <decrypt>true</decrypt>

        <git>
            <!-- Ejecutable git instaldo o portable en la pc,
            Si no lo tienen por defecto es jgit (Git Java)-->
            <exec>C:/tools/git/bin/git.exe</exec>
            <!-- Parametro para habilitar el push automatico en cada (default true)
            "mvn clean install" 0 "mvn jgitcrypt:jgitcrypt"-->
            <autopush>false</autopush>
            <!-- Parametro solo valido con ejecutable git-->
            <debug>false</debug>
            <!-- Repositorio git -->
            <url>http://github.com/danielnavas11/00000.git</url>
            <!-- Credenciales del git -->
            <credentials>
                <user>danielnavas11</user>
                <password>passssssss</password>
            </credentials>
        </git>
        <!-- Para crear las keys (Publica y privada) si ya existen no las crea de nuevo y firma los
        archivos java y los sube al git
        <createKey>
            <pathKeys>${project.build.sourceDirectory}</pathKeys>
            <id>Daniel Navas Sanchez</id>
            <password>passssssss</password>
            <armor>true</armor>
        </createKey>-->
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>jgitcrypt</goal>
            </goals>
        </execution>
    </executions>
    
</plugin>            