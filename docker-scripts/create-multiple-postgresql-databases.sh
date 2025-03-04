#!/bin/bash

set -e  # Exit on error
set -u  # Treat unset variables as errors

function create_user_and_database() {
    local database="$1"
    echo "  Creating user and database '$database'"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname=postgres <<-EOSQL
        CREATE DATABASE "$database";
        GRANT ALL PRIVILEGES ON DATABASE "$database" TO "$POSTGRES_USER";
EOSQL
}

if [ -n "${POSTGRES_MULTIPLE_DATABASES-}" ]; then
    echo "Multiple database creation requested: $POSTGRES_MULTIPLE_DATABASES"
    
    # Use IFS (Internal Field Separator) to split the string properly
    IFS=',' read -ra DBS_ARRAY <<< "$POSTGRES_MULTIPLE_DATABASES"
    
    for db in "${DBS_ARRAY[@]}"; do
        create_user_and_database "$db"
    done

    echo "Multiple databases created"
fi
