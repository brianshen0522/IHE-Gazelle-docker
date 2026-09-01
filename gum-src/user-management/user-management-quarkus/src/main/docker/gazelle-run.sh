#!/bin/bash

# Database management
if [ "${DB_ENABLED:-true}" = "true" ]; then
    export PGPASSWORD=${DB_GUM_PASSWORD}

    # Check if db host is reachable
    while ! pg_isready -h $DB_GUM_HOST --port ${DB_GUM_PORT} > /dev/null 2> /dev/null;
    do
      echo "⚠ Waiting 5s for database server to be reachable ${DB_GUM_HOST}:${DB_GUM_PORT}..."
      sleep 5
    done
    echo "✓ Database server is accessible"

    # Check if database exists
    psql -h ${DB_GUM_HOST} --port ${DB_GUM_PORT} -U ${DB_GUM_USER} ${DB_GUM_NAME} -c '\q' >/dev/null 2>/dev/null
    if [ $? -ne 0 ]; then
        # Create DB if not exists
        echo "⚠ Create database ${DB_GUM_NAME}..."
        createdb -U ${DB_GUM_USER} --port ${DB_GUM_PORT} -h ${DB_GUM_HOST} -E UTF-8 ${DB_GUM_NAME}
    fi
fi


# Run the script
/opt/jboss/container/java/run/run-java.sh