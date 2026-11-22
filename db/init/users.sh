#!/bin/sh
set -e
mysql --protocol=socket -uroot -p"${MYSQL_ROOT_PASSWORD}" <<SQL
CREATE USER IF NOT EXISTS 'cert_migrator'@'%' IDENTIFIED BY '${DB_MIGRATOR_PASSWORD}';
GRANT ALL PRIVILEGES ON jg_service.* TO 'cert_migrator'@'%';
CREATE USER IF NOT EXISTS 'cert_app'@'%' IDENTIFIED BY '${DB_APP_PASSWORD}';
GRANT SELECT, INSERT, UPDATE, DELETE ON jg_service.* TO 'cert_app'@'%';
FLUSH PRIVILEGES;
SQL
