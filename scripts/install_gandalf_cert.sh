JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home

sudo keytool -import -trustcacerts -alias gandalf-expired -file gandalf.crt -keystore "$JAVA_HOME/jre/lib/security/cacerts"
