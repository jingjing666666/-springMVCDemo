package com.jingjing.annotion;

import java.lang.annotation.*;

/**
 * @Author: 020188
 * @Date: 2019/7/3
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyRequestParam {
    /**
     * 参数的别名
     * @return
     */
    String value() default "";
}
