
if [ "$2" = "server" ];
then
	java $1.server.Server
elif [ "$2" = "client" ];
then
	java $1.client.Client
    # In Milestone3 changes Client to ClientUI
else
	echo "Must specify client or server"
fi