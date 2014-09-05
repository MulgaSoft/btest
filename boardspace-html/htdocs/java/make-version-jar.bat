jar -cmf online/manifest.txt online/version.jar online/*.class 
jarsigner -storepass davescommodocert -storetype pkcs12 -keystore g:/share/usr/ddyer/crypto/Boardspace.p12  online/version.jar "david dyer's comodo ca limited id"
