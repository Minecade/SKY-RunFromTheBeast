#!/bin/bash
SERVER_COUNT=30
for ((INDEX=1; INDEX <= $SERVER_COUNT; INDEX++)) do
	cd "/home/gr$INDEX"
	screen -d -m -S gr$INDEX ./run.sh
	echo "Started server #$INDEX"
done
