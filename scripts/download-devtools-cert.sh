echo "Downloading artifactory certificate"

openssl s_client -servername devtools.hq.sodep.com.py -connect devtools.hq.sodep.com.py:443 </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' >devtools.crt

echo "Download done"
