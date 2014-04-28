jar -cmf online/manifest.txt online/version.jar online/*.class 
jarsigner -storepass BSkeystore -keystore g:/share/usr/ddyer/crypto/Boardspace.keystore  online/version.jar "david dyer's comodo ca limited id"
g:/share/usr/ddyer/crypto/Boardspace.keystore ../v102/%%f "david dyer's comodo ca limited id"
