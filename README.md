# Hopefully_Unhackable - SSE Client

## How to run:
1. Install Java 8 if not already installed
  ```
    $ sudo add-apt-repository ppa:webupd8team/java
    $ sudo apt-get update
    $ sudo apt-get install oracle-java8-installer
  ```
Accept the terms when prompted

2. Get source code and switch into directory
  ```
    $ git clone https://github.com/lmtrombone/EncryptedClient.git
    $ cd EncryptedClient
  ```

3. Replace security policy jars with provided ones
  ```
    $ sudo cp policy_jars/* /usr/lib/jvm/java-8-oracle/jre/lib/security/
  ```
  *This path may vary depending on location of java installation*

4. Added our self-signed SSL certificate to Java
  Download the certificate for the server, and navigate to folder.
  ```
    $ keytool -import -alias grum -file derek -keystore /usr/lib/jvm/java-8-oracle/jre/lib/security/cacerts
  ```
  The default password is "changeit"
  
  *This path may vary depending on location of java installation*

5. Create AWS credentials files
  ```
    $ mkdir ~/.aws
    $ cat > ~/.aws/credentials
  ```
    Paste credential information

    Ctrl + D to finish

6. Run client
  ```
    ./gradlew run
  ```
