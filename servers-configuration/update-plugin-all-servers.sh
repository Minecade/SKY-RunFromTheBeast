#!/bin/bash
SERVER_COUNT=90
for ((INDEX=1; INDEX <= $SERVER_COUNT; INDEX++)) do
	rm -rf "/home/RB$INDEX/plugins/update/minecade-run-from-the-beast*.jar"
	echo "Removed plugin on server #$INDEX"
	cp /home/minecade-run-from-the-beast*.jar "/home/RB$INDEX/plugins/update/"
	echo "Copied plugin on server #$INDEX"
done