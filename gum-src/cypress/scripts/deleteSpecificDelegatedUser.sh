#!/bin/bash

USER_EMAIL=$1
psql -U gazelle -h localhost -v emailUser="'$USER_EMAIL'" -d gum -f ./delete_gum_user.sql
psql -U gazelle -h localhost -v emailUser="'$USER_EMAIL'" -d keycloak -f ./delete_keycloak_broker_user_by_broker_username.sql