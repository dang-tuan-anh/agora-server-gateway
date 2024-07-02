#!/bin/bash

mvn package
java -Djava.library.path=$PATH:native/linux/x86_64 -cp `find target -name *.jar -printf %p:` com.example.Main