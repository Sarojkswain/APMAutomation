REM usage: sql_export DATABASE_FOLDER
SET host=localhost
SET schema=cemdb
SET user=postgres

echo \copy apm_vertex TO '%~dp0apm_vertex.csv' csv header |%1\bin\psql.exe -h %host% -p 5432 -f - %schema% %user%
echo \copy apm_owner TO '%~dp0apm_owner.csv' csv header |%1\bin\psql.exe -h %host% -p 5432 -f - %schema% %user%
echo \copy apm_agent TO '%~dp0apm_agent.csv' csv header |%1\bin\psql.exe -h %host% -p 5432 -f - %schema% %user%
echo \copy apm_vertex_type TO '%~dp0apm_vertex_type.csv' csv header |%1\bin\psql.exe -h %host% -p 5432 -f - %schema% %user%
echo \copy apm_edge TO '%~dp0apm_edge.csv' csv header |%1\bin\psql.exe -h %host% -p 5432 -f - %schema% %user%
