#!/bin/bash
SERVER_COUNT=90
for ((INDEX=1; INDEX <= $SERVER_COUNT; INDEX++)) do
	rm -rf "/home/RB$INDEX/plugins/update/NoCheatPlus*.jar"
	echo "Removed NoCheatPlus on server #$INDEX"
	cp /home/NoCheatPlus*.jar "/home/RB$INDEX/plugins/"
	echo "Copied NoCheatPlus on server #$INDEX"
done
