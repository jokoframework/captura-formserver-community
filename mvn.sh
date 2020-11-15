# export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Hom/e/jre


# export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_111.jdk/Contents/Home/jre

# MAVEN_OPTS="-Djavax.net.ssl.keyStore=$JAVA_HOME/lib/security/cacerts -Djavax.net.ssl.keyStorePassword=changeit -Djavax.net.ssl.trustStore=$JAVA_HOME/lib/security/cacerts -Djavax.net.ssl.trustStorePassword=changeit"

mvn -s ~/.m2/sodep-settings.xml "$@"


