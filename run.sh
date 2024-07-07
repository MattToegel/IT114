#!/bin/bash
# Convert input to lowercase
input=$(echo "$2" | tr '[:upper:]' '[:lower:]')
if [ "$input" = "server" ];
then
	java $1.Server.Server
elif [ "$input" = "client" ];
then
	java $1.Client.Client
    # In Milestone3 changes Client to ClientUI
else
	echo "Must specify client or server"
fi