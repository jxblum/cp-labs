#!/bin/bash

testname="$1"
testname="${testname:-SpringBootJsonWebApplicationGatlingSimulation}"

rm -Rf target/gatling
mvn gatling:test -Dtest.simulationClass=examples.gatling.$testname
