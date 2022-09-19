#!/bin/bash

# Output timestamp
echo "$(date +'%H:%M:%S.%3N') Call jar file"

# Check whether crac-files folder is present not empty and if so, restore from checkpoint, otherwise start from jar
if [ -d "/opt/crac-files" ]; then
  if [ "$(ls -A /opt/crac-files)" ]; then
    echo "Restore from checkpoint"
    java -XX:CRaCRestoreFrom=/opt/crac-files
  else
    echo "Standard start from jar file"
    java -XX:CRaCCheckpointTo=/opt/crac-files -jar /opt/app/crac4-17.0.0.jar
  fi
else
  echo "No crac-files folder found"
  mkdir /opt/crac-files
  echo "Standard start from jar file"
  java -XX:CRaCCheckpointTo=/opt/crac-files -jar /opt/app/crac4-17.0.0.jar
fi
