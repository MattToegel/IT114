cd $1
find -name "*.java" > sources.txt
javac @sources.txt
cd ..
# Make build directory inside project folder if not exists
mkdir -p $1/build
# create a server jar in the build folder, specify the entry point (which class runs), add the class fields to the class-path (server and common packages)
jar cfe $1/build/$1_Server.jar $1.server.Server $1/server/*.class $1/common/*.class
# create a server jar in the build folder, specify the entry point (which class runs), add the class fields to the class-path (client and common packages)
jar cfe $1/build/$1_Client.jar $1.client.ClientUI $1/client/*.class $1/client/views/*.class $1/common/*.class
# c means we're creating a file, f means the directory/file to generate, e means the entry point of the generated file
echo "java -jar  $1_Client.jar" > $1/build/run.bat
echo "PAUSE" >> $1/build/run.bat
