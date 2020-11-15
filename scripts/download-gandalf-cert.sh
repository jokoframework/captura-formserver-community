echo "Downloading artifactory certificate"

openssl s_client -servername gandalf.sodep.com.py -connect gandalf.sodep.com.py:443 </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' >gandalf.crt

echo "Download done"
