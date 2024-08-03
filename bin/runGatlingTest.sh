#!/bin/bash

testname="$1"
testname="${testname:-SpringBootJsonWebApplicationGatlingSimulation}"

rm -Rf terget/gatling
mvn gatling:test -Dtest.simulationClass=examples.gatling.$testname
