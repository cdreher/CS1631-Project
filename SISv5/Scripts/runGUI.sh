#!/bin/bash
javac -sourcepath ../Components/GUI -cp "../Components/*" ../Components/GUI/*.java
cd ../Components/GUI;
java -cp ".:../*" CreateGUI;
