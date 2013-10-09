package cocaine.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cocaine.CocaineDeserializer;
import cocaine.CocaineSerializer;
import cocaine.MessagePackDeserializer;
import cocaine.MessagePackSerializer;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CocaineMethod {

    String value() default "";

    Class<? extends CocaineSerializer> serializer() default MessagePackSerializer.class;

    Class<? extends CocaineDeserializer> deserializer() default MessagePackDeserializer.class;

}
