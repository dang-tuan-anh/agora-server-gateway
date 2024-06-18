#!/bin/bash

mvn package
java -cp `find target -name *.jar -printf %p:` com.example.Main