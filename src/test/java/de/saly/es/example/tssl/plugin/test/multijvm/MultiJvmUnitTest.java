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

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import de.saly.es.example.tssl.util.ConfigConstants;
import de.saly.es.example.tssl.util.SecurityUtil;

public class MultiJvmUnitTest {

    @Rule
    public TestRule watcher = new TestWatcher() {
        @Override
        protected void starting(final Description description) {
            System.out.println("---------------------------------------------------------------------------");
            System.out.println("Starting test: " + description.getMethodName());
            System.out.println("---------------------------------------------------------------------------");
            System.out.println();
        }
    };

    private final List<Process> processes = new ArrayList<>();

    protected NodeClient nodeClient;
    private Node node;

    public void startCluster() throws Exception {
        final String separator = System.getProperty("file.separator");
        final String classpath = System.getProperty("java.class.path");
        final String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
        final ProcessBuilder processBuilder = new ProcessBuilder(path, "-Xmx1g", "-Xms1g", "-cp", classpath,
                Cluster.class.getCanonicalName());
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(Redirect.INHERIT);
        processes.add(processBuilder.start());
    }

    public void killCluster() {

        if (node != null) {
            node.close();
        }

        for (final Iterator iterator = processes.iterator(); iterator.hasNext();) {
            final Process process = (Process) iterator.next();
            try {
                process.destroy();
            } catch (final Throwable e) {
                //ignore
            }
        }

    }

    public void wipe() {
        try {
            FileUtils.deleteDirectory(new File("data"));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    protected void initClient() {
        final Settings s = ImmutableSettings.builder().put("cluster.name", "test_cluster").put(sslSettings()).build();

        node = new NodeBuilder().settings(s).client(true).local(false).build();
        nodeClient = (NodeClient) node.start().client();
    }

    protected Settings sslSettings() {
        final Settings localSettings = settingsBuilder()
                .put(ConfigConstants.SECURITY_SSL_TRANSPORT_NODE_ENABLED, true)
                .put(ConfigConstants.SECURITY_SSL_TRANSPORT_NODE_KEYSTORE_FILEPATH,
                        SecurityUtil.getAbsoluteFilePathFromClassPath("node-" + 1 + "-keystore.jks"))
                .put(ConfigConstants.SECURITY_SSL_TRANSPORT_NODE_TRUSTSTORE_FILEPATH,
                        SecurityUtil.getAbsoluteFilePathFromClassPath("truststore.jks"))
                .put(ConfigConstants.SECURITY_SSL_TRANSPORT_NODE_ENCFORCE_HOSTNAME_VERIFICATION, false)
                .put(ConfigConstants.SECURITY_SSL_TRANSPORT_NODE_TRUSTSTORE_PASSWORD, "tspass")
                .put(ConfigConstants.SECURITY_SSL_TRANSPORT_NODE_KEYSTORE_PASSWORD, "kspass").build();
        return localSettings;
    }

    protected void waitForNodes(int count) {
        count++; //internal node client counts as node
        initClient();
        boolean clusterOk = false;
        ClusterHealthResponse healthResponse = null;

        do {
            try {
                healthResponse = nodeClient.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
                final int numberOfNodes = healthResponse.getNumberOfNodes();
                System.out.println("Number of nodes up: " + numberOfNodes);
                if (numberOfNodes < count) {
                    System.out.println("Not all nodes up yet (need at least " + count + ")");
                    Thread.sleep(5000);
                    continue;
                }
                clusterOk = true;
            } catch (final Exception e) {
                System.out.println("Warning " + e.getMessage());
                try {
                    Thread.sleep(5000);
                } catch (final InterruptedException e1) {
                }
            }
        } while (!clusterOk);

        System.out.println("Cluster state ok, there are " + healthResponse.getNumberOfNodes() + " nodes up ("
                + healthResponse.getNumberOfDataNodes() + " are data nodes)");

    }

}
