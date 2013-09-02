#!/bin/bash

SERVER_COUNT=90
for ((INDEX=1; INDEX <= $SERVER_COUNT; INDEX++)) do
	cd "/home/RB$INDEX"
	screen -d -m -S RB$INDEX ./run.sh
	echo "Started server #$INDEX"
done
