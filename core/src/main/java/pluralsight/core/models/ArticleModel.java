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
package pluralsight.core.models;

import com.adobe.granite.jmx.annotation.Name;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.settings.SlingSettingsService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

@Model(adaptables=Resource.class)
public class ArticleModel {

    public ArticleModel(Resource resource) {
        // Get Current Articles Path
        this.artPath = resource.getPath();

        // Get Current Articles Image
        Resource imageRes = resource.getChild("root/image");
        this.image = imageRes.getValueMap().get("fileReference", String.class);
    }

    //Need to get the resource.
    private String artPath;

    @ValueMapValue
    @Named("jcr:title")
    @Required
    private String title;

    @Inject
    @Named("feedDesc")
    private String desc;

    private String image;

    @PostConstruct
    protected void init() {
        this.artPath = this.artPath.replace("/jcr:content","");
    }

}
