wd=$(pwd)
echo $wd
cd $1
find . -name "*.java" > sources.txt
javac @sources.txt
cd "$wd"