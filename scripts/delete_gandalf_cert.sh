JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home

sudo keytool -delete -alias gandalf-expired -keystore "$JAVA_HOME/jre/lib/security/cacerts"
