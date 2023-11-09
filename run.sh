port=3000
if [ -n "$3" ];
then
	port="$3"
fi
if [ "$2" = "server" ];
then
	echo "port $port"
	java $1.server.Server $port
elif [ "$2" = "client" ];
then
	java $1.client.Client
    # In Milestone3 changes Client to ClientUI
elif [ "$2" = "ui" ];
then
	java $1.client.ClientUI
else
	echo "Must specify client or server"
fi