package com.jingjing.annotion;

import java.lang.annotation.*;

/**
 * @Author: 020188
 * @Date: 2019/7/3
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyController {

    /**
     * 表示给controller注册别名
     * @return
     */
    String value() default "";
}
