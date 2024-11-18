#!/bin/bash
# Convert input to lowercase
input=$(echo "$2" | tr '[:upper:]' '[:lower:]')
port=${3:-3000}  # Default port to 3000 if not 
# Default debug mode to false
debug=false
debugArg=""
# Check for -d flag
if [[ " $@ " =~ " -d " ]]; then
    debug=true
fi
if $debug; then
    debugArg="-agentlib:jdwp=transport=dt_socket,server=y,address=5005"
    echo "Debug mode is ON"
fi

if [ "$input" = "server" ]; then
    java $debugArg $1.Server.Server $port 
elif [ "$input" = "client" ]; then
    java $debugArg $1.Client.Client
    # In Milestone3 changes Client to ClientUI
elif [ "$input" = "ui" ]; then
	java $debugArg $1.Client.ClientUI
	# Milestone 3's new entry point
else
    echo "Must specify client or server"
fi

