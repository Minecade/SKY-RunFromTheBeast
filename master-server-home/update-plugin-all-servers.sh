#!/bin/bash
SERVER_COUNT=30
for ((INDEX=1; INDEX <= $SERVER_COUNT; INDEX++)) do
	rm -rf "/home/gr$INDEX/plugins/minecade-gravity-run*.jar"
	echo "Removed plugin on server #$INDEX"
	cp /home/minecade-gravity-run*.jar "/home/gr$INDEX/plugins/"
	echo "Copied plugin on server #$INDEX"
done