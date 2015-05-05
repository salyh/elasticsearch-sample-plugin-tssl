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

import junit.framework.Assert;

import org.elasticsearch.action.admin.cluster.node.stats.NodeStats;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsRequest;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.junit.Test;

import de.saly.es.example.tssl.plugin.test.multijvm.MultiJvmUnitTest;

public class SecurityTest extends MultiJvmUnitTest {

    @Test
    public void testSSLFail() throws Exception {

        TransportClient transportClient = null;

        try {
            wipe();
            startCluster();
            waitForNodes(3);

            nodeClient.prepareIndex("test", "testtype").setSource("{\"a\":1}").setRefresh(true).get();
            final NodesStatsResponse stats = nodeClient.admin().cluster().nodesStats(new NodesStatsRequest()).actionGet();

            final Settings s = ImmutableSettings.builder().put("cluster.name", "test_cluster").put("plugins.load_classpath_plugins", false)
                    .build();
            transportClient = new TransportClient(s);

            for (final NodeStats stat : stats.getNodes()) {
                transportClient.addTransportAddress(stat.getNode().address());
            }

            try {
                transportClient.prepareIndex("test", "testtype").setSource("{\"a\":1}").setRefresh(true).get();
                Assert.fail("Expected a NoNodeAvailableException because of org.elasticsearch.common.netty.handler.ssl.NotSslRecordException: not an SSL/TLS record");
            } catch (final NoNodeAvailableException e) {
                //expected
            }

        } finally {

            if (transportClient != null) {
                transportClient.close();
            }

            killCluster();
        }
    }

    @Test
    public void testSSL() throws Exception {

        TransportClient transportClient = null;

        try {
            wipe();
            startCluster();
            waitForNodes(3);

            nodeClient.prepareIndex("test", "testtype").setSource("{\"a\":1}").setRefresh(true).get();
            final NodesStatsResponse stats = nodeClient.admin().cluster().nodesStats(new NodesStatsRequest()).actionGet();

            final Settings s = ImmutableSettings.builder().put("cluster.name", "test_cluster").put("plugins.load_classpath_plugins", true)
                    .put(sslSettings()) // <----
                    .build();
            transportClient = new TransportClient(s);

            for (final NodeStats stat : stats.getNodes()) {
                transportClient.addTransportAddress(stat.getNode().address());
            }

            final IndexResponse resp = transportClient.prepareIndex("test", "testtype").setSource("{\"a\":1}").setRefresh(true).get();
            Assert.assertNotNull(resp.getId());

        } finally {

            if (transportClient != null) {
                transportClient.close();
            }

            killCluster();
        }
    }

}
