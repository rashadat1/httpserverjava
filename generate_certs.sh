#!/bin/bash

set -e

# Clean up old certs
rm -rf certs
mkdir -p certs/ca certs/server certs/client

### Step 1: Generate CA
echo "Generating CA private key and certificate..."
openssl genrsa -out certs/ca/ca.key 4096
openssl req -x509 -new -nodes -key certs/ca/ca.key -sha256 -days 3650 \
-subj "/C=US/ST=Massachusetts/L=Boston/O=AvoTest/OU=Engineering/CN=ca.avotest.local/emailAddress=trashada1@gmail.com" \
-out certs/ca/ca.crt

### Step 2: Generate Server Private Key and CSR
echo "Generating server private key and CSR..."
openssl genrsa -out certs/server/server.key 4096
openssl req -new -key certs/server/server.key -out certs/server/server.csr \
-subj "/C=US/ST=Massachusetts/L=Boston/O=AvoTest/OU=Engineering/CN=localhost/emailAddress=trashada1@gmail.com"

### Step 3: Sign Server Certificate with CA
echo "Signing server certificate with CA..."
openssl x509 -req -in certs/server/server.csr -CA certs/ca/ca.crt -CAkey certs/ca/ca.key \
-CAcreateserial -out certs/server/server.crt -days 825 -sha256

# Create Server Keystore (server-keystore.p12)
echo "Creating server keystore..."
openssl pkcs12 -export -in certs/server/server.crt -inkey certs/server/server.key \
-certfile certs/ca/ca.crt -out certs/server/server-keystore.p12

### Step 4: Generate Client Private Key and CSR
echo "Generating client private key and CSR..."
openssl genrsa -out certs/client/client.key 4096
openssl req -new -key certs/client/client.key -out certs/client/client.csr \
-subj "/C=US/ST=Massachusetts/L=Boston/O=AvoTest/OU=Engineering/CN=localhost/emailAddress=trashada1@gmail.com"

### Step 5: Sign Client Certificate with CA
echo "Signing client certificate with CA..."
openssl x509 -req -in certs/client/client.csr -CA certs/ca/ca.crt -CAkey certs/ca/ca.key \
-out certs/client/client.crt -days 825 -sha256

# Create Client Keystore (client-keystore.p12)
echo "Creating client keystore..."
openssl pkcs12 -export -in certs/client/client.crt -inkey certs/client/client.key \
-certfile certs/ca/ca.crt -out certs/client/client-keystore.p12

### Step 6: Create Truststore (ca-truststore.p12)
echo "Creating CA truststore..."
keytool -importcert -trustcacerts -alias ca-cert -file certs/ca/ca.crt \
-keystore certs/ca/ca-truststore.p12 -storetype PKCS12

echo ""
echo "✅ Certificates, keystores, and truststore generated successfully!"
