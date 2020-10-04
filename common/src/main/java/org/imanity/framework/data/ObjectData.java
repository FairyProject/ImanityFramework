package org.imanity.framework.data;

import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
public class ObjectData extends AbstractData {

    public ObjectData(UUID uuid) {
        super(uuid);
    }

}
