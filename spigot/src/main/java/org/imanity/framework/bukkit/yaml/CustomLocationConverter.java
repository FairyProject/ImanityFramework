package org.imanity.framework.bukkit.yaml;

import org.imanity.framework.bukkit.util.CustomLocation;
import org.imanity.framework.config.Converter;

public class CustomLocationConverter implements Converter<CustomLocation, String> {
    @Override
    public String convertTo(CustomLocation element, ConversionInfo info) {
        return element.toString();
    }

    @Override
    public CustomLocation convertFrom(String element, ConversionInfo info) {
        return CustomLocation.stringToLocation(element);
    }
}
