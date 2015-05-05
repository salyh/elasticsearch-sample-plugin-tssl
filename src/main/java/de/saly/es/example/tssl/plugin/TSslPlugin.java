/*
Copyright 2015 Hendrik Saly

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package de.saly.es.example.tssl.plugin;

import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;
import org.elasticsearch.transport.TransportModule;

import de.saly.es.example.tssl.netty.SSLNettyTransport;
import de.saly.es.example.tssl.netty.UnexpectedSecurityException;
import de.saly.es.example.tssl.rest.TSslRestAction;
import de.saly.es.example.tssl.util.ConfigConstants;

public class TSslPlugin extends AbstractPlugin {

    private final Settings settings;

    public TSslPlugin(final Settings settings) {
        this.settings = settings;
    }

    @Override
    public String name() {
        return "elasticsearch-sample-plugin-tssl";
    }

    @Override
    public String description() {
        return "Elasticsearch example plugin which implements and enforces transport layer SSL/TLS encryption";
    }

    public void onModule(final TransportModule transportModule) {
        transportModule.setTransport(SSLNettyTransport.class, name());
    }

    public void onModule(final RestModule restModule) {
        restModule.addRestAction(TSslRestAction.class);
    }

    @Override
    public Settings additionalSettings() {
        final ImmutableSettings.Builder settingsBuilder = ImmutableSettings.settingsBuilder();
        if (settings.getAsBoolean(ConfigConstants.SECURITY_SSL_TRANSPORT_NODE_ENABLED, false)) {
            final String keystoreFilePath = settings.get(ConfigConstants.SECURITY_SSL_TRANSPORT_NODE_KEYSTORE_FILEPATH,
                    System.getProperty("javax.net.ssl.keyStore", null));
            final String truststoreFilePath = settings.get(ConfigConstants.SECURITY_SSL_TRANSPORT_NODE_TRUSTSTORE_FILEPATH,
                    System.getProperty("javax.net.ssl.trustStore", null));

            if (StringUtils.isBlank(keystoreFilePath) || StringUtils.isBlank(truststoreFilePath)) {
                throw new UnexpectedSecurityException(ConfigConstants.SECURITY_SSL_TRANSPORT_NODE_KEYSTORE_FILEPATH + " and "
                        + ConfigConstants.SECURITY_SSL_TRANSPORT_NODE_TRUSTSTORE_FILEPATH + " must be set if transport ssl is reqested.");
            }
        }
        return settingsBuilder.build();
    }
}
