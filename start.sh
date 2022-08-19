#!/bin/bash

# Output timestamp
echo "$(date +'%H:%M:%S.%3N') Call jar file"

# Output epoch seconds before starting the application
# java -XX:+UnlockDiagnosticVMOptions -Djdk.crac.trace-startup-time=true -XX:+CRTraceStartupTime -XX:CRaCCheckpointTo=/home/hansolo/crac-files -jar build/libs/crac4-17.0.0.jar

# Output compilation log that can be analyzed with JITWatch
# java -XX:CRaCCheckpointTo=/home/hansolo/crac-files -XX:+UnlockDiagnosticVMOptions -XX:+LogCompilation -jar build/libs/crac4-17.0.0.jar

# Normal run without additional options
java -XX:CRaCCheckpointTo=/home/hansolo/crac-files -jar build/libs/crac4-17.0.0.jar