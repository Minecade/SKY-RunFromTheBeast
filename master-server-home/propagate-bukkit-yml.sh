#!/bin/bash
SERVER_COUNT=90
for ((INDEX=1; INDEX <= $SERVER_COUNT; INDEX++)) do
    cp master-bukkit.yml RB$INDEX/bukkit.yml
    echo "Propagated bukkit.yml to server #$INDEX"
done


