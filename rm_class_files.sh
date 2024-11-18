#!/bin/bash
wd=$(pwd)
cd $1
# delete all .class files
find . -name "*.class" -type f -delete