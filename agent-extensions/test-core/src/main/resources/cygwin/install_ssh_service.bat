@echo OFF

set INSTALL_HOME=%1
set USER=%2
set PASSWORD=%3

:: delete sshd service if it already exists
sc query | find /I "sshd" > nul
if not errorlevel 1 echo. && net stop sshd && sc delete sshd
if errorlevel 1 echo.   - Windows service sshd is not running or doesn't exist. Creating cygwin sshd...

sc query | find /I "ssh-agent" > nul
if not errorlevel 1 echo. && net stop ssh-agent && sc delete ssh-agent

:: create cygwin ssdh service
%INSTALL_HOME%\bin\bash --login -c "setfacl -b /var; chown :USERS /var; chmod 755 /var; chmod ug-s /var; chmod +t /var; /bin/ssh-host-config -y -c ntsec -u %USER% -w %PASSWORD%"

%INSTALL_HOME%\bin\bash --login -c "chmod 777 /etc/sshd_config; echo 'KexAlgorithms diffie-hellman-group1-sha1' >> /etc/sshd_config"

net start sshd
