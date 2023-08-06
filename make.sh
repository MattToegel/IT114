#!/bin/bash

# Check if the correct number of arguments (1) is provided
if [ $# -ne 1 ]; then
  echo "Usage: $0 <project_directory>"
  exit 1
fi

# Store the current working directory
pwd=$(pwd)

# Change to the project directory
project_dir="$1"
cd "$project_dir"

# Find and compile Java source files
echo "Compiling Java source files..."
find . -name "*.java" > sources.txt
javac @sources.txt

# Check if Java compilation was successful
if [ $? -ne 0 ]; then
  echo "Error: Java compilation failed."
  rm sources.txt
  exit 1
fi

# Move back to the original working directory
cd "$pwd"

# Create the build directory if it doesn't exist
mkdir -p "$project_dir/build"

# Build the server JAR
echo "Creating the server JAR..."
server_classpath="$project_dir/server:$project_dir/common"
jar cfe "$project_dir/build/${project_dir}_Server.jar" "${project_dir}.server.Server" $(find "$project_dir" -name "*.class")

# Check if the server JAR creation was successful
if [ $? -ne 0 ]; then
  echo "Error: Failed to create the server JAR."
  exit 1
fi

# Build the client JAR
echo "Creating the client JAR..."
client_classpath="$project_dir/client:$project_dir/client/views:$project_dir/common"
jar cfe "$project_dir/build/${project_dir}_Client.jar" "${project_dir}.client.ClientUI" $(find "$project_dir" -name "*.class")

# Check if the client JAR creation was successful
if [ $? -ne 0 ]; then
  echo "Error: Failed to create the client JAR."
  exit 1
fi

# Create the run script for Windows (run.bat) or Unix (run.sh)
echo "Creating the run script..."
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
  echo "java -jar ${project_dir}_Client.jar" > "$project_dir/build/run_client.bat"
else
  echo "java -jar ${project_dir}_Client.jar" > "$project_dir/build/run_client.sh"
  chmod +x "$project_dir/build/run_client.sh"
fi

# Clean up the generated sources.txt file
rm "$project_dir/sources.txt"

echo "Build completed successfully."