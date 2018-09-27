#!/bin/bash

#usage: sql_export DATABASE_FOLDER
host=localhost
schema=cemdb
user=postgres

current_dir=`pwd`

echo "copy apm_vertex      TO '$current_dir/apm_vertex.csv'      csv header" | $1/bin/psql -h $host -p 5432 -f - $schema -U $user
echo "copy apm_owner       TO '$current_dir/apm_owner.csv'       csv header" | $1/bin/psql -h $host -p 5432 -f - $schema -U $user
echo "copy apm_agent       TO '$current_dir/apm_agent.csv'       csv header" | $1/bin/psql -h $host -p 5432 -f - $schema -U $user
echo "copy apm_vertex_type TO '$current_dir/apm_vertex_type.csv' csv header" | $1/bin/psql -h $host -p 5432 -f - $schema -U $user
echo "copy apm_edge        TO '$current_dir/apm_edge.csv'        csv header" | $1/bin/psql -h $host -p 5432 -f - $schema -U $user
