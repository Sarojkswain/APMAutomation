# What is this?

This is a very simple Java agent that allows us to
set Win32 console window title.

# How to use this?

Get the `win32-helper-java-agent-*-jar-with-dependencies.jar`
artifact and copy it close to your console application/utility.
Modify your `java` invocation command line, add two new arguments:

- `-javaagent:win32-helper-java-agent-99.99.aquarius-SNAPSHOT-jar-with-dependencies.jar`
- `"-Dwin32.console.title=testing Win32 console title"`

The title is taken from the `win32.console.title` property and it is
_prepended_ to the existing console title.

# How is this useful?

This helps us with process identification when we want
to kill it. Using this utility Java agent we can add
a unique string into the console window title and then use
`taskkill` with `WINDOWTITLE` filter to find it and kill it.

Setting the console window title is also possible using the
`title` command in a batch file. However using a batch file
is inconvenient.

# Is there anything else it can do?

Actually, yes, there is more.

## `win32.console.hide`

You can use `win32.console.hide` property set to `true` value
to hide console window. Even when the console window is hidden,
it can be found by `taskkill`.
