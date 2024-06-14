#!/bin/bash

mvn package
java -cp target/agora-example-1.0-SNAPSHOT.jar:target/lib/linux-sdk-3.7.200.21.jar com.example.Main