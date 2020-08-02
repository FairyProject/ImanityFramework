package org.imanity.framework.bukkit.util;

import org.bukkit.metadata.FixedMetadataValue;
import org.imanity.framework.bukkit.Imanity;

public class SampleMetadata extends FixedMetadataValue {
    public SampleMetadata(Object value) {
        super(Imanity.PLUGIN, value);
    }
}
