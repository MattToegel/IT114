if [ "$2" = "server" ];
then
	java $1.Server.Server
elif [ "$2" = "client" ];
then
	java $1.Client.Client
    # Milestone 2 runner
elif [ "$2" = "ui" ];
then
	java $1.Client.ClientUI
    # Milestone 3 runner
else
	echo "Must specify client or server"
fi