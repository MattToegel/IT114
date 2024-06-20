#!/bin/bash
wd=$(pwd)
cd $1
# delete all .class files
find . -name "*.class" -type f -delete
find . -name "*.java" > sources.txt
javac @sources.txt
cd "$wd"