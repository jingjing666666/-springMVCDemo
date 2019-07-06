package com.jingjing.annotion;

import java.lang.annotation.*;

/**
 * @Author: 020188
 * @Date: 2019/7/3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
@Documented
public @interface MyRequestMapping {
    /**
     * 表示访问该方法的URL
     * @return
     */
    String value() default "";
}
