cd $1
find -name "*.java" > sources.txt
javac @sources.txt
cd ..
# Make build directory inside project folder if not exists
mkdir -p $1/build
# create a server jar in the build folder, specify the entry point (which class runs), add the class fields to the class-path (server and common packages)
jar cfe $1/build/$1_Server.jar $1.Server.Server $1/Server/*.class $1/Common/*.class
# create a server jar in the build folder, specify the entry point (which class runs), add the class fields to the class-path (client and common packages)
jar cfe $1/build/$1_Client.jar $1.Client.ClientUI $1/Client/*.class $1/Client/Views/*.class $1/Common/*.class $1/Server/*.class
# c means we're creating a file, f means the directory/file to generate, e means the entry point of the generated file
echo "java -jar  $1_Client.jar" > $1/build/run.bat
echo "PAUSE" >> $1/build/run.bat
