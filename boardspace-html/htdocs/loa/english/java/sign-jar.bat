jar -cmf loa-manifest.txt LoaLib.jar loa\player\*.class dlib\*.class sgf\*.class loa\common\*.class loa\ui\*.class loa\viewer\*.class sgf\*.class 
jar -i LoaLib.jar 
jarsigner -storepass BSkeystore -keystore ../../../java/v101/boardspace.keystore LoaLib.jar "david dyer's comodo ca limited id"
