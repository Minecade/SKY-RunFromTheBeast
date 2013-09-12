#!/bin/bash
crontab -l | { cat; echo "5 0 * * * /home/clean-logs-task.sh"; } | crontab -