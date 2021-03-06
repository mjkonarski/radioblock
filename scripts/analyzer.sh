#!/bin/bash

MUTE=10

rtl_fm -M wbfm -f 89.5M -g 0 \
 | /opt/jdk8/bin/java -jar analyzer-1.0.0-SNAPSHOT.jar RATE_32_MONO 80 90 \
 | while read line
do
  if [ "$line" == "Got jingle 0" ]; then
    OLD_VOLUME=$(./yamaha.sh volume)
    ./yamaha.sh volume $MUTE > /dev/null
    echo "$(date) Setting volume to $MUTE"
  elif [ "$line" == "Got jingle 1" ]; then
    NEW_VOLUME=$(./yamaha.sh volume)
    if [ "$NEW_VOLUME" -eq "$MUTE" ]; then
      ./yamaha.sh volume $OLD_VOLUME > /dev/null
      echo "$(date) Restoring volume to $OLD_VOLUME"
    else
      echo "$(date) Volume has been updated manually to $NEW_VOLUME"
    fi
  fi
done
