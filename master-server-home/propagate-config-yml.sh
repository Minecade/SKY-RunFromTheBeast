#!/bin/bash
SERVER_COUNT=90
for ((INDEX=1; INDEX <= $SERVER_COUNT; INDEX++)) do
    cp master-config.yml RB$INDEX/plugins/minecade-run-from-the-beast/config.yml
    sed -i "s/<index>/$INDEX/g" RB$INDEX/plugins/minecade-run-from-the-beast/config.yml
    echo "Propagated config.yml to server #$INDEX"
done
