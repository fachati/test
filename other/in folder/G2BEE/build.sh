./gradlew assembleDebug -PSTORE_FILE=/home/hanyu/Desktop/g2mobility.keystore -PSTORE_PASSWORD="6#%!m+e6F<*K;aaR" -PKEY_ALIAS=g2mobility -PKEY_PASSWORD="2TR[z,:9yuz\vm<A"
adb remount
adb push app/build/outputs/apk/app-debug.apk /system/app/g2bee.apk
adb shell pm install -r /system/app/g2bee.apk
adb shell am start -n com.g2mobility.xbee/.G2BeeActivity