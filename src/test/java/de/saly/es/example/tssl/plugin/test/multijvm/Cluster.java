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

package de.saly.es.example.tssl.plugin.test.multijvm;

import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;

import de.saly.es.example.tssl.util.ConfigConstants;
import de.saly.es.example.tssl.util.SecurityUtil;

public class Cluster {

    public static void main(final String[] args) {

        nodeBuilder().settings(settings(0, true, true)).node();

        nodeBuilder().settings(settings(1, true, true)).node();

        nodeBuilder().settings(settings(2, false, true)).node();

    }

    private static Builder settings(final int nodeOrdinal, final boolean dataNode, final boolean masterNode) {
        final Builder settings = ImmutableSettings.settingsBuilder().put("node.data", dataNode).put("node.master", masterNode)
                .put("cluster.name", "test_cluster").put("gateway.type", "none").put("path.data", "data/data")
                .put("path.work", "data/work").put("path.logs", "data/logs").put("path.conf", "data/config")
                .put("index.number_of_shards", "3").put("index.number_of_replicas", "1").put("http.enabled", true)
                .put("http.cors.enabled", true).put("network.tcp.connect_timeout", 60000).put("node.local", false)
                .put(sslSettings(nodeOrdinal));

        //System.out.println(settings.build().getAsMap());
        return settings;
    }

    private static Settings sslSettings(final int nodeOrdinal) {
        final Settings localSettings = settingsBuilder()
                .put(ConfigConstants.SECURITY_SSL_TRANSPORT_NODE_ENABLED, true)
                .put(ConfigConstants.SECURITY_SSL_TRANSPORT_NODE_KEYSTORE_FILEPATH,
                        SecurityUtil.getAbsoluteFilePathFromClassPath("node-" + nodeOrdinal + "-keystore.jks"))
                .put(ConfigConstants.SECURITY_SSL_TRANSPORT_NODE_TRUSTSTORE_FILEPATH,
                        SecurityUtil.getAbsoluteFilePathFromClassPath("truststore.jks"))
                .put(ConfigConstants.SECURITY_SSL_TRANSPORT_NODE_ENCFORCE_HOSTNAME_VERIFICATION, false)
                .put(ConfigConstants.SECURITY_SSL_TRANSPORT_NODE_TRUSTSTORE_PASSWORD, "tspass")
                .put(ConfigConstants.SECURITY_SSL_TRANSPORT_NODE_KEYSTORE_PASSWORD, "kspass").build();
        return localSettings;
    }
}
