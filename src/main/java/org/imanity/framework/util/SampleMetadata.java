package org.imanity.framework.util;

import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.imanity.framework.Imanity;

public class SampleMetadata extends FixedMetadataValue {

    public SampleMetadata(Object value) {
        super(Imanity.PLUGIN, value);
    }
}
