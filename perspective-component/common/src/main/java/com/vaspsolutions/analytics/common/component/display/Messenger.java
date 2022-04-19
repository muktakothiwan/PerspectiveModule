package com.vaspsolutions.analytics.common.component.display;

import javax.swing.ImageIcon;

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema;
import com.inductiveautomation.perspective.common.api.ComponentDescriptor;
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl;
import com.vaspsolutions.analytics.common.Analytics;


/**
 * Common meta information about the Messenger component.  See {@link Image} for docs on each field.
 */
public class Messenger {

    public static String COMPONENT_ID = "rad.display.messenger";


    public static JsonSchema SCHEMA =
        JsonSchema.parse(Analytics.class.getResourceAsStream("/messenger.props.json"));

    public static ComponentDescriptor DESCRIPTOR = ComponentDescriptorImpl.ComponentBuilder.newBuilder()
        .withPaletteCategory(Analytics.COMPONENT_CATEGORY)
        .withPaletteDescription("A component that uses component messaging and data fetching delegates.")
        .withId(COMPONENT_ID)
        .withModuleId(Analytics.MODULE_ID)
        .withSchema(SCHEMA) //  this could alternatively be created purely in Java if desired
        .withPaletteName("Gateway Messenger")
        .withDefaultMetaName("messenger")
        .shouldAddToPalette(true)
        .withResources(Analytics.BROWSER_RESOURCES)
        .build();
}
