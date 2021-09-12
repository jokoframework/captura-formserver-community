#!/bin/bash

DIRS="simplecaptcha sodep-export-utilities swagger-springmvc-community \
	gandalf-community license-community \
	captura-exchange-community captura-form_definitions-community \
	captura-formserver-community"		
BASE_DIR="/home/intruder/git/captura-community"
if [ -z ${MOBILEFORMS_HOME} ]; then
	echo "Please define MOBILEFORMS_HOME according to RUN.md"
	exit 122
fi
for D in $DIRS; do 
	cd $BASE_DIR/$D
	pwd
	mvn -DskipTests -Dmaven.javadoc.skip=true install
	if [ $? -ne 0 ]; then
		exit 123
	fi
done

