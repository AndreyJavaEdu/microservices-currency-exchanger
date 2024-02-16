#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE account_history;
    GRANT ALL PRIVILEGES ON DATABASE  account_history TO postgres;
EOSQL