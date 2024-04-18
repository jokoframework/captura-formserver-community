#!/bin/bash

MOBILEFORMS_HOME=/srv/workspace/configuration/slot0/profile
SEED_FILE=db/seed_workflow_tigo.sql
PROPERTY_FILE=$MOBILEFORMS_HOME/jdbc.properties 


function prop {
    grep "${1}" $PROPERTY_FILE|cut -d'=' -f2
}

USERNAME="$(prop 'jdbc.username')"
PASSWORD="$(prop 'jdbc.password')"
DB="mf_slot0_demo"
HOST="localhost"
PORT=5432

export PGPASSWORD=$PASSWORD
set -x
psql -h $HOST -p $PORT -U $USERNAME -d $DB -f $SEED_FILE

