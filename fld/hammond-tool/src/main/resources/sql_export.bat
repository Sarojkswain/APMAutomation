REM usage: sql_export DATABASE_FOLDER
SET host=localhost
SET schema=cemdb
SET user=postgres

echo \copy (SELECT query_to_xml('SELECT * FROM apm_vertex', true, false, '')) TO '%~dp0apm_vertex.bin' WITH BINARY |%1\bin\psql.exe -h %host% -p 5432 -f - %schema% %user%
echo \copy (SELECT query_to_xml('SELECT * FROM apm_owner', true, false, '')) TO '%~dp0apm_owner.bin' WITH BINARY |%1\bin\psql.exe -h %host% -p 5432 -f - %schema% %user%
echo \copy (SELECT query_to_xml('SELECT * FROM apm_agent', true, false, '')) TO '%~dp0apm_agent.bin' WITH BINARY |%1\bin\psql.exe -h %host% -p 5432 -f - %schema% %user%
echo \copy (SELECT query_to_xml('SELECT * FROM apm_vertex_type', true, false, '')) TO '%~dp0apm_vertex_type.bin' WITH BINARY |%1\bin\psql.exe -h %host% -p 5432 -f - %schema% %user%
echo \copy (SELECT query_to_xml('SELECT * FROM apm_edge', true, false, '')) TO '%~dp0apm_edge.bin' WITH BINARY |%1\bin\psql.exe -h %host% -p 5432 -f - %schema% %user%

REM FOR %%I IN (0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30) DO echo \copy (SELECT query_to_xml('SELECT * FROM apm_edge ORDER BY id LIMIT 1000000 OFFSET %%I000000', true, false, '')) TO '%~dp0apm_edge_%%I.bin' WITH BINARY |%1\bin\psql.exe -h %host% -p 5432 -f - %schema% %user%

