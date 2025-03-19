package net.devgrr.interp.ia.api.config.swagger.annotation;

import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * org.springframework.web.bind.annotation.RequestBody
 * io.swagger.v3.oas.annotations.parameters.RequestBody
 * 구분하기 위해 어노테이션 재정의
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@RequestBody
@Inherited
public @interface SwaggerBody {

  @AliasFor(annotation = RequestBody.class)
  String description() default "";

  @AliasFor(annotation = RequestBody.class)
  Content[] content() default {};

  @AliasFor(annotation = RequestBody.class)
  boolean required() default false;

  @AliasFor(annotation = RequestBody.class)
  Extension[] extensions() default {};

  @AliasFor(annotation = RequestBody.class)
  String ref() default "";

  @AliasFor(annotation = RequestBody.class)
  boolean useParameterTypeSchema() default false;
}
