package org.imanity.framework.mysql.pojo;

import lombok.Getter;
import lombok.Setter;
import org.imanity.framework.ObjectSerializer;

import javax.persistence.AttributeConverter;
import javax.persistence.Column;
import javax.persistence.EnumType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Getter
@Setter
public class Property {

	private String name;

	private Method readMethod;
	private Method writeMethod;

	private Field field;

	private Class<?> dataType;

	private boolean generated;
	private boolean primaryKey;
	private boolean enumField;

	private Class<Enum> enumClass;
	private EnumType enumType;

	private Column columnAnnotation;

	private ObjectSerializer serializer;
	private AttributeConverter converter;

}
