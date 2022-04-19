package com.vaspsolutions.analytics.common.component.display;

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema;
import com.inductiveautomation.perspective.common.api.ComponentDescriptor;
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl;
import com.vaspsolutions.analytics.common.Analytics;

/**
 * Meta information about the TagCounter component.  See {@link Image} for docs on each field.
 */
public class TagCounter {
    public static String COMPONENT_ID = "rad.display.tagcounter";

    public static JsonSchema SCHEMA =
        JsonSchema.parse(Analytics.class.getResourceAsStream("/tagcounter.props.json"));

    public static ComponentDescriptor DESCRIPTOR = ComponentDescriptorImpl.ComponentBuilder.newBuilder()
        .withPaletteCategory(Analytics.COMPONENT_CATEGORY)
        .withPaletteDescription("A component that displays the number of tags associated with a gateway.")
        .withId(COMPONENT_ID)
        .withModuleId(Analytics.MODULE_ID)
        .withSchema(SCHEMA) //  this could alternatively be created purely in Java if desired
        .withPaletteName("Tag Counter")
        .withDefaultMetaName("tagCounter")
        .shouldAddToPalette(true)
        .withResources(Analytics.BROWSER_RESOURCES)
        .build();

}
