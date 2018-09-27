openssl genrsa 2048 > private.pem

openssl req -x509 -days 10000 -new -key private.pem -out public.pem -verbose -subj "/C=CZ/ST=Czech Republic/L=Prague/O=CA/OU=System Test/CN=www.ca.com"

openssl pkcs12 -export -in public.pem -inkey private.pem -out agentCertificate.pfx -password pass:password
