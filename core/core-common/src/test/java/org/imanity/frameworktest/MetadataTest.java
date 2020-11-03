package org.imanity.frameworktest;

import org.imanity.framework.metadata.CommonMetadataRegistries;
import org.imanity.framework.metadata.MetadataKey;
import org.imanity.framework.metadata.MetadataMap;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class MetadataTest {

    @Test
    public void testMetadata() {
        // Make int metadata key
        MetadataKey<Integer> intKey = MetadataKey.createIntegerKey("test");

        // Put into uuid cache
        UUID testUuid = UUID.randomUUID();

        MetadataMap map = CommonMetadataRegistries.provide(testUuid);
        map.put(intKey, 20);

        // Assert
        assertEquals(map, CommonMetadataRegistries.provide(testUuid));
        assertEquals(CommonMetadataRegistries.provide(testUuid).getOrNull(intKey), (Object) 20);

        // Remove the metadata we just put
        CommonMetadataRegistries.provide(testUuid).remove(intKey);

        // Assert Removal for Metadata
        assertNull(CommonMetadataRegistries.provide(testUuid).getOrNull(intKey));

        CommonMetadataRegistries.remove(testUuid);

        // Assert Removal for Metadata Map
        assertNull(CommonMetadataRegistries.getOrNull(testUuid));
    }

}
