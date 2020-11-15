JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home

sudo keytool -v -import -trustcacerts -alias devtools -file devtools.crt -keystore "$JAVA_HOME/jre/lib/security/cacerts"
