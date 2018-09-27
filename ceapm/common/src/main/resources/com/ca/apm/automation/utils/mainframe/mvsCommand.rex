/* REXX */
/* Issues a MVS command passed in parameters through SDSF */

parse arg cmd
irc = isfcalls('ON')
if (irc <> 0) then return 1000+irc

/* Execute command and write execution information and its output to stdout */
address sdsf "ISFEXEC /'" || strreplace(cmd, "'", "''") || "'"
erc = rc
/* SDSF short message */
if (ISFMSG <> "ISFMSG") then do
    say ISFMSG
end
/* SDSF long message */
do i = 1 to ISFMSG2.0
    say ISFMSG2.i
end
/* Command response */
do i = 1 to ISFULOG.0
    say ISFULOG.i
end

irc = isfcalls('OFF')
if (irc <> 0) then return 2000+irc

return erc

strreplace:
    str = arg(1)
    target = arg(2)
    replacement = arg(3)
    begin = 1
    ret = ''
    do while pos(target, str, begin) > 0
        targetpos = pos(target, str, begin)
        ret = ret || substr(str, begin, targetpos-begin) || replacement
        begin = targetpos + length(target)
    end
    return ret || substr(str, begin)
