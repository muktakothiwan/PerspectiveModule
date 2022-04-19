package com.vaspsolutions.analytics.common.component.display;



import com.inductiveautomation.ignition.common.jsonschema.JsonSchema;
import com.inductiveautomation.perspective.common.api.ComponentDescriptor;
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl;

import com.vaspsolutions.analytics.common.Analytics;



/**
 * Describes the component to the Java registry so the gateway and designer know to look for the front end elements.
 * In a 'common' scope so that it's referencable by both gateway and designer.
 */
public class Image  {

    // unique ID of the component which perfectly matches that provided in the javascript's ComponentMeta implementation
    public static String COMPONENT_ID = "Analytics";

    /**
     * The schema provided with the component descriptor. Use a schema instead of a plain JsonObject because it gives
     * us a little more type information, allowing the designer to highlight mismatches where it can detect them.
     */
    public static JsonSchema SCHEMA =
        JsonSchema.parse(Analytics.class.getResourceAsStream("/radimage.props.json"));

    /**
     * Components register with the Java side ComponentRegistry but providing a ComponentDescriptor.  Here we
     * build the descriptor for this one component. Icons on the component palette are optional.
     */
    public static ComponentDescriptor DESCRIPTOR = ComponentDescriptorImpl.ComponentBuilder.newBuilder()
        .withPaletteCategory(Analytics.COMPONENT_CATEGORY)
        .withPaletteDescription("An analytics component.")
        .withId(COMPONENT_ID)
        .withModuleId(Analytics.MODULE_ID)
        .withSchema(SCHEMA) //  this could alternatively be created purely in Java if desired
        .withPaletteName("Analytics Module")
        .withDefaultMetaName("Analytics Module")
        .shouldAddToPalette(true)
        .withResources(Analytics.BROWSER_RESOURCES)
        .build();

}
