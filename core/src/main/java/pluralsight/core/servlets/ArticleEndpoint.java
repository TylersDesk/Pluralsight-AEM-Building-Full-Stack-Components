/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package pluralsight.core.servlets;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

/**
 * Servlet that writes some sample content into the response. It is mounted for
 * all resources of a specific Sling resource type. The
 * {@link SlingSafeMethodsServlet} shall be used for HTTP methods that are
 * idempotent. For write operations use the {@link SlingAllMethodsServlet}.
 */
@Component(service=Servlet.class,
           property={
                   Constants.SERVICE_DESCRIPTION + "=Article Json Servlet",
                   "sling.servlet.methods=" + HttpConstants.METHOD_GET,
                   "sling.servlet.resourceTypes=" + "full-stack-training/components/structure/page",
                   "sling.servlet.selectors=articles",
                   "sling.servlet.extensions=json"
           })
public class ArticleEndpoint extends SlingSafeMethodsServlet {

    private static final long serialVersionUid = 1L;

    //ResourceType of our article pages
    private static String ARTICLE_RESOURCE_TYPE = "full-stack-training/components/structure/page-article";

    //Properties we will be reading from the JCR
    private static String FEED_DESCRIPTION_PROP = "feedDesc";
    private static String FEED_IMAGE_PROP = "fileReference";
    private static String FEED_TITLE_PROP = "jcr:title";
    private static String IMAGE_RESOURCE_PATH = "root/image";


    @Reference
    private QueryBuilder queryBuilder;

    @Override
    protected void doGet(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {

        final Resource resource = req.getResource();

        //Set the response type header to JSON
        resp.setContentType("application/json");

        final List<Resource> resources = new ArrayList<Resource>();

        //Map to build queryBuilder
        Map<String, String> map = new HashMap<String, String>();
        map.put("path", resource.getParent().getPath());
        map.put("1_property","sling:resourceType");
        map.put("1_property.value", ARTICLE_RESOURCE_TYPE);
        //Suggested always use
        map.put("p.guessTotal", "true");

        //Get a resource resolver
        ResourceResolver resourceResolver = req.getResourceResolver();

        //Query articles
        Query query = queryBuilder.createQuery(PredicateGroup.create(map), resourceResolver.adaptTo(Session.class));
        SearchResult result = query.getResult();

        // QueryBuilder has a leaking ResourceResolver, so the following work around is required.
        ResourceResolver leakingResourceResolver = null;

        try {
            // A common use case it to collect all the resources that represent hits and put them in a list for work outside of the search service
            for (final Hit hit : result.getHits()) {
                if (leakingResourceResolver == null) {
                    // Get a reference to QB's leaking ResourceResolver
                    leakingResourceResolver = hit.getResource().getResourceResolver();
                }

                // Add all of the pages we found in our query to a resources List.
                resources.add(resourceResolver.getResource(hit.getPath()));
            }

        } catch (RepositoryException e) {
        } finally {
            if (leakingResourceResolver != null) {
                // Always Close the leaking QueryBuilder resourceResolver.
                leakingResourceResolver.close();
            }
        }

        //Create a new JsonArray to store json objects in, this is what we will pass back from our endpooint.
        JsonArray jsonArray = new JsonArray();

        //Iterate over the resources in our list from the query.
        Iterator<Resource> resourceIterator= resources.iterator();
        while (resourceIterator.hasNext()) {
            Resource currentResource = resourceIterator.next();
            ValueMap vm = currentResource.getValueMap();
            JsonObject json = new JsonObject();
            json.addProperty("title", vm.get(FEED_TITLE_PROP,""));
            json.addProperty("artPath", currentResource.getPath());
            json.addProperty("desc", vm.get(FEED_DESCRIPTION_PROP, ""));

            //Have to do something a little different for our image path.
            Resource imageResource = currentResource.getChild(IMAGE_RESOURCE_PATH);
            String imageRef = imageResource.getValueMap().get(FEED_IMAGE_PROP, "");
            json.addProperty("image", imageRef);

            // Add our json object to the json array
            jsonArray.add(json);
        }

        Gson gson = new Gson();
        resp.getWriter().write(gson.toJson(jsonArray));
    }

    String getString() {
        return "hello";
    }
}
