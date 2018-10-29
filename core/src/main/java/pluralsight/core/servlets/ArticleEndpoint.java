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


import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.*;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


import pluralsight.core.models.ArticleModel;

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


    //ResourceType of our article pages
    private static String ARTICLE_RESOURCE_TYPE = "full-stack-training/components/structure/page-article";

    @Reference
    private QueryBuilder queryBuilder;


    @Override
    protected void doGet(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {
        
        //Set the response type header to JSON
        resp.setContentType("application/json; charset=UTF-8");
        
                
        // Get the resource from where the request path is
        final Resource resource = req.getResource();

        // Get a resourceResolver from the request! - Awesome!
        ResourceResolver resourceResolver = req.getResourceResolver();

        //Map to build queryBuilder
        Map<String, String> map = new HashMap<String, String>();
        map.put("path", resource.getParent().getPath());
        map.put("1_property","sling:resourceType");
        map.put("1_property.value", ARTICLE_RESOURCE_TYPE);
        map.put("p.guessTotal", "true"); //Suggested always use
        map.put("p.limit","-1"); //Don't limit to 10 results.

        //Query articles - Requires a resource resolver which we got from the request above
        Query query = queryBuilder.createQuery(PredicateGroup.create(map), resourceResolver.adaptTo(Session.class));
        SearchResult result = query.getResult();

        // Create an List to hold all the found article page resources        
        final List<Resource> resources = new ArrayList<Resource>();

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
            // Should log the caught exception
        } finally {
            if (leakingResourceResolver != null) {
                // Always Close the leaking QueryBuilder resourceResolver.
                leakingResourceResolver.close();
            }
        }

        //Create an ArrayList to store objects in, we will turn this to JSON with GSON.
        ArrayList<ArticleModel> allArticles = new ArrayList<>();

        //Iterate over the resources in our list from the query.
        Iterator<Resource> resourceIterator= resources.iterator();
        while (resourceIterator.hasNext()) {

            //Grab the current Resource/Article
            Resource currentResource = resourceIterator.next();

            //Adapt it to our Sling Model (aka make it an article!)
            ArticleModel currenArticle = currentResource.adaptTo(ArticleModel.class);

            //Add it to our array.
            allArticles.add(currenArticle);
        }


        //Create the JSON string
        String responseJson = new Gson().toJson(allArticles);

        // Finally send the JSON as the response of our servlet!
        resp.getWriter().write(responseJson);


    }
}
