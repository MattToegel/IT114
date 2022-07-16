if [ "$1" = "server" ];
then
	java LifeForLife.server.Server
elif [ "$1" = "client" ];
then
	java LifeForLife.client.ClientUI
else
	echo "Must specify client or server"
fi
