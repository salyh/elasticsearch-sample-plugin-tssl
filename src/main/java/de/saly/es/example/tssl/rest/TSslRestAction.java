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

package de.saly.es.example.tssl.rest;

import java.io.IOException;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestRequest.Method;
import org.elasticsearch.rest.RestStatus;

import de.saly.es.example.tssl.util.SecurityUtil;

public class TSslRestAction extends BaseRestHandler {

    @Inject
    public TSslRestAction(final Settings settings, final Client client, final RestController controller) {
        super(settings, controller, client);
        controller.registerHandler(Method.GET, "/_tssl/state", this);
        controller.registerHandler(Method.POST, "/_tssl/state", this);

    }

    @Override
    protected void handleRequest(final RestRequest request, final RestChannel channel, final Client client) throws Exception {

        if (request.param("cat") != null) {
            final StringBuilder sb = new StringBuilder();
            sb.append("enabled protocols\n");
            for (final String protocol : SecurityUtil.ENABLED_SSL_PROTOCOLS) {
                sb.append(protocol + "\n");
            }

            sb.append("\nenabled ciphers\n");
            for (final String cipher : SecurityUtil.ENABLED_SSL_CIPHERS) {
                sb.append(cipher + "\n");
            }

            sb.append("\nunlimited strength policy installed\n");
            sb.append(SecurityUtil.UNLIMITED_STRENGTH_SUPPORTED);
            channel.sendResponse(new BytesRestResponse(RestStatus.OK, "text/plain", sb.toString()));
            return;
        }

        try {
            final XContentBuilder builder = JsonXContent.contentBuilder();
            builder.startObject();
            builder.field("enabled_protocols", SecurityUtil.ENABLED_SSL_PROTOCOLS);
            builder.field("enabled_chipers", SecurityUtil.ENABLED_SSL_CIPHERS);
            builder.field("unlimited_strength_policy_installed", SecurityUtil.UNLIMITED_STRENGTH_SUPPORTED);
            builder.endObject();
            channel.sendResponse(new BytesRestResponse(RestStatus.OK, builder));
        } catch (final IOException e) {
            try {
                channel.sendResponse(new BytesRestResponse(channel, e));
            } catch (final IOException e1) {
                logger.error("Failed to send a failure response.", e1);
            }
        }

    }

}
