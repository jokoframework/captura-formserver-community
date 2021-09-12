#!/bin/bash

REPOS="https://github.com/jokoframework/simplecaptcha.git \
	https://github.com/jokoframework/sodep-export-utilities.git \
	https://github.com/jokoframework/swagger-springmvc-community.git \
	https://github.com/jokoframework/gandalf-community.git \
	https://github.com/jokoframework/license-community.git \
	https://github.com/jokoframework/captura-exchange-community.git \
	https://github.com/jokoframework/captura-form_definitions-community.git \
	https://github.com/jokoframework/captura-formserver-community.git"
CAPTURA_SOURCES_DIR="captura-sources"

if [ ! -d "$CAPTURA_SOURCES_DIR" ]; then
	mkdir -v $CAPTURA_SOURCES_DIR
fi
cd $CAPTURA_SOURCES_DIR

for R in $REPOS; do
	echo "Clonning $R"
	sleep 1
	git clone $R
done


