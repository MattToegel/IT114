if [ "$1" = "server" ];
then
	java AnteMatter.server.Server
elif [ "$1" = "client" ];
then
	java AnteMatter.client.ClientUI
else
	echo "Must specify client or server"
fi
