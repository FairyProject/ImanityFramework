package org.imanity.framework.mysql;

import org.imanity.framework.Autowired;
import org.imanity.framework.PostInitialize;
import org.imanity.framework.Repository;
import org.imanity.framework.ServiceDependency;

@ServiceDependency(dependencies = "sql")
public abstract class SQLRepository<T, ID> implements Repository<T, ID> {

    @Autowired
    protected SQLService sqlService;

    @PostInitialize
    public void init() {
        //TODO: build table, pojo information
    }

}
