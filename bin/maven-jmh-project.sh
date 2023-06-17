#!/bin/bash

mvn archetype:generate \
 -DinteractiveMode=false \
 -DarchetypeGroupId=org.openjdk.jmh \
 -DarchetypeArtifactId=jmh-java-benchmark-archetype \
 -DgroupId=org.example \
 -DartifactId=example-benchmark \
 -Dversion=0.1.0-SNAPSHOT
