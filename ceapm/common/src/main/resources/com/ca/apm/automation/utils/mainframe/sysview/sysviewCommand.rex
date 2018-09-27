/* REXX */
/* Issues a SYSVIEW command passed in parameters */

parse arg cmd

address 'LINK' 'GSVXRXAA'

/* API Configuration */
address 'SYSVIEWE' "C(set APIDlmchar |)"
address 'SYSVIEWE' "C(set APIXltblanks MAXIMUM)"
do while queued() > 0
    parse pull line
end

/* Command execution and output */
address 'SYSVIEWE' "C("||cmd||")"
erc = rc
do while queued() > 0
    parse pull line
    say line
end

return erc