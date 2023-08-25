package com.github.devnied.emvnfccard.parser.apdu.annotation;

import com.github.devnied.emvnfccard.utils.BitUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Data {

    String format() default BitUtils.DATE_FORMAT;

    int dateStandard() default 0;

    int index();

    boolean readHexa() default false;

    int size();

    String tag() default "";
}
