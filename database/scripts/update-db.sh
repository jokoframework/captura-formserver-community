#!/bin/bash

if [ -z ${MOBILEFORMS_HOME} ]; then 
	echo -n "MOBILEFORMS_HOME not defined, exporting default: "
	export MOBILEFORMS_HOME=/srv/workspace/configuration/slot0/profile
fi

echo "Using MOBILEFORMS_HOME: ${MOBILEFORMS_HOME}"

mvn org.codehaus.mojo:properties-maven-plugin:read-project-properties liquibase:update
