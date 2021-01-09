package org.imanity.framework.mysql.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Where {

    private final String property;
    private final Object value;

}
