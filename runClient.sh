JAVA="/usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java"
CONFIG='-Dlogback.configurationFile="./config/logback.xml"'

CP="-cp target/classes/:target/dependency/*"

$JAVA $CONFIG $CP wallet.client.WalletInteractiveClient $@
