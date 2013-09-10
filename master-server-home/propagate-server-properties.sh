#!/bin/bash
SERVER_COUNT=90
for ((INDEX=1; INDEX <= $SERVER_COUNT; INDEX++)) do
    cp master-server.properties RB$INDEX/server.properties
    port=`expr 10000 + $INDEX`
    sed -i "s/<port>/$port/g" RB$INDEX/server.properties
    echo "Propagated to server #$INDEX"
done
