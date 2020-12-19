package org.imanity.framework.details;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import org.imanity.framework.BeanContext;
import org.imanity.framework.ServiceDependency;
import org.imanity.framework.details.constructor.BeanConstructorDetails;
import org.imanity.framework.details.constructor.GenericBeanConstructorDetails;

import java.util.Arrays;
import java.util.Set;

@Getter
@Setter
public class ServiceBeanDetails extends GenericBeanDetails {

    private Set<String> dependencies;
    private BeanConstructorDetails constructorDetails;

    public ServiceBeanDetails(Class<?> type, String name, String[] dependencies) {
        super(type, name);

        this.dependencies = Sets.newHashSet(dependencies);
    }

    public void setupConstruction(BeanContext beanContext) {
        this.constructorDetails = new GenericBeanConstructorDetails(this.getType(), beanContext);
        for (Class<?> parameters : this.constructorDetails.getParameterTypes()) {
            BeanDetails details = beanContext.getBeanDetails(parameters);

            this.dependencies.add(details.getName());
        }
    }

    public void build(BeanContext context) {
        if (this.constructorDetails == null) {
            throw new IllegalArgumentException("The construction for bean details " + this.getType().getName() + " hasn't been called!");
        }

        this.setInstance(this.constructorDetails.newInstance(context));
    }

    public boolean hasDependencies() {
        return this.dependencies.size() > 0;
    }

    @Override
    public void loadAnnotations() {
        super.loadAnnotations();

        Class<?> type = this.getType();
        while (type != null && type != Object.class) {
            ServiceDependency dependencyAnnotation = type.getAnnotation(ServiceDependency.class);
            if (dependencyAnnotation != null) {
                dependencies.addAll(Arrays.asList(dependencyAnnotation.dependencies()));
            }

            type = type.getSuperclass();
        }
    }
}
