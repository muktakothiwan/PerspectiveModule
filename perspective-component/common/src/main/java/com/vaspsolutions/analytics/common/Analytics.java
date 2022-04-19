package com.vaspsolutions.analytics.common;

import java.util.Set;

import com.google.common.collect.Sets;
import com.inductiveautomation.perspective.common.api.BrowserResource;

public class Analytics {

    public static final String MODULE_ID = Constants.MODULE_ID;
    public static final String URL_ALIAS = "analytics";
    public static final String COMPONENT_CATEGORY = "Analytics Module";
    public static final Set<BrowserResource> BROWSER_RESOURCES =
        Sets.newHashSet(
            new BrowserResource(
                "analytics-js",
                String.format("/res/%s/Analytics.js", URL_ALIAS),
                BrowserResource.ResourceType.JS
            ),
            new BrowserResource("analytics-css",
                String.format("/res/%s/Analytics.css", URL_ALIAS),
                BrowserResource.ResourceType.CSS
            )
        );
}
