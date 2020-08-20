package org.imanity.framework.data.type;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Field;

@AllArgsConstructor
@Data
public class DataFieldConvert {

    private String name;
    private DataConverterType type;
    private Field field;

}
