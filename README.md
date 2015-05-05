elasticsearch-sample-plugin-tssl
================================

Elasticsearch example plugin which implements and enforces transport layer SSL/TLS encryption

Setup is done in elasticsearch.yml
<pre>
###################################### Security ####################################

# Enable or disable transport layer security (default: false)
#security.ssl.transport.node.enabled: true

# Enable or disable SSL hostname verfification (default: true)
#security.ssl.transport.node.hostname_verification.enabled: false

# Enable or disable hostname resolving for hostname verfification (default: true)
# This settings has no effect if hostname_verification is disabled
#security.ssl.transport.node.hostname_verification.resolve_host_name: false

# If true then the nodes have to trust each other, if false then only the client node has to trust the server node
# (default: true)
#security.ssl.transport.node.need_clientauth: false

# Path to the keystore (default is value of javax.net.ssl.keyStore)
# This setting is mandatory if javax.net.ssl.keyStore is not set
#security.ssl.transport.node.keystore.path: null

# Keystore password (default is value of javax.net.ssl.keyStorePassword or 'changeit' if not set)
#security.ssl.transport.node.keystore.password: changeit

# Keystore type (default is value of javax.net.ssl.keyStoreType or 'JKS' if not set)
# JKS or PKCS12 are supported
#security.ssl.transport.node.keystore.type: PKCS12

# Path to the truststore (default is value of javax.net.ssl.trustStore)
# This setting is mandatory if javax.net.ssl.trustStore is not set
# Can be the same as security.ssl.transport.node.keystore.path
#security.ssl.transport.node.truststore.path: null

# Truststore password (default is value of javax.net.ssl.trustStorePassword or 'changeit' if not set)
#security.ssl.transport.node.truststore.password: changeit

# Truststore type (default is value of javax.net.ssl.trustStoreType or 'JKS' if not set)
# JKS or PKCS12 are supported
#security.ssl.transport.node.truststore.type: PKCS12

# SSL context cache size (default: 1000)
# A value of zero means unlimited (WARNING: this can cause memory leaks)
#security.ssl.transport.node.session.cache_size: 1000

# SSL context session timeout expressed in seconds (default: 86400, which means 24h)
# A value of zero means unlimited (WARNING: this can cause memory leaks)
#security.ssl.transport.node.session.timeout: 86400
</pre>

For setting up the keystore and truststore together with a certificate authority (CA) look here:
<code>
pki-scripts/example.sh
</code>
Using a root CA make it possible to add new nodes to the cluster without a cluster restart.
 
<h3>License</h3> 
Copyright (C) 2015 Hendrik Saly

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing permissions
and limitations under the License.
