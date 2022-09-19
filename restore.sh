#!/bin/bash

# Output epoch seconds before restoring the application
#date +%s

##java -XX:+UnlockDiagnosticVMOptions -Djdk.crac.trace-startup-time=true -XX:+CRTraceStartupTime -XX:CRaCRestoreFrom=/home/hansolo/crac-files/
java -XX:CRaCRestoreFrom=/home/hansolo/crac-files
