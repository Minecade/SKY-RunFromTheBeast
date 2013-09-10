#!/bin/bash

SERVER_COUNT=90
for ((INDEX=1; INDEX <= $SERVER_COUNT; INDEX++)) do
	rm -rf RB$INDEX/HaloRaceWorld
	cp -R worlds/HaloRaceWorld RB$INDEX/
	echo "Replaced HaloRaceWorld world on server #$INDEX"
done
