package org.imanity.framework.factory;

import lombok.Getter;
import org.imanity.framework.Autowired;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class FieldFactory {

    private static final Map<Class<? extends Annotation>, AnnotatedFieldHolder> ANNOTATED_FIELDS = new HashMap<>();

    public static void loadFields() {
        Reflections reflections = ClassFactory.REFLECTIONS;

        FieldFactory.scan(Autowired.class, reflections);
    }

    public static Collection<Field> getStaticFields(Class<? extends Annotation> annotation) {
        if (ANNOTATED_FIELDS.containsKey(annotation)) {
            return ANNOTATED_FIELDS.get(annotation).getStaticFields();
        }

        return Collections.emptyList();
    }

    public static Collection<Field> getFields(Class<? extends Annotation> annotation, Class<?> type) {
        if (ANNOTATED_FIELDS.containsKey(annotation)) {
            AnnotatedFieldHolder fieldHolder = ANNOTATED_FIELDS.get(annotation);
            return fieldHolder.getFields(type);
        }

        return Collections.emptyList();
    }

    private static void scan(Class<? extends Annotation> annotation, Reflections reflections) {
        Set<Field> fields = reflections.getFieldsAnnotatedWith(annotation);
        AnnotatedFieldHolder fieldHolder = new AnnotatedFieldHolder(fields);

        ANNOTATED_FIELDS.put(annotation, fieldHolder);
    }

    public static class AnnotatedFieldHolder {

        @Getter
        private final Collection<Field> staticFields;
        private final Map<Class<?>, Collection<Field>> nonStaticFields;

        private AnnotatedFieldHolder(Set<Field> fields) {
            this.staticFields = new ArrayList<>();
            this.nonStaticFields = new HashMap<>();

            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    this.staticFields.add(field);
                } else {
                    Collection<Field> collection;
                    Class<?> type = field.getDeclaringClass();

                    if (nonStaticFields.containsKey(type)) {
                        collection = nonStaticFields.get(type);
                    } else {
                        collection = new ArrayList<>();
                        nonStaticFields.put(type, collection);
                    }

                    collection.add(field);
                }
            }
        }

        public Collection<Field> getFields(Class<?> type) {
            return this.nonStaticFields.getOrDefault(type, Collections.emptyList());
        }

    }

}
