#!/bin/bash

PGPASSWORD=gazelle psql -U gazelle -h localhost -v emailUser="'harry.covert@gmail.com'" -d gum -f delete_gum_user.sql
PGPASSWORD=gazelle psql -U gazelle -h localhost -v emailUser="'valentine.lorande@kereval.com'" -d gum -f delete_gum_user.sql
PGPASSWORD=gazelle psql -U gazelle -h localhost -v emailUser="'banne-gaelle@ihe-europe.net'" -d gum -f delete_gum_user.sql
PGPASSWORD=gazelle psql -U gazelle -h localhost -v idpAlias="'DELEG_Mock'" -d keycloak -f delete_keycloak_broker_user.sql