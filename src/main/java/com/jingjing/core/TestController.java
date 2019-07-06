package com.jingjing.core;

import com.jingjing.annotion.MyController;
import com.jingjing.annotion.MyRequestMapping;
import com.jingjing.annotion.MyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@MyController
@MyRequestMapping(value = "/test")
public class TestController {

    @MyRequestMapping("/doTest1")
    public void test1(HttpServletRequest request, HttpServletResponse response,
                      @MyRequestParam("param") String param){
        System.out.println(param);

        try {
            response.getWriter().write("doTest method success! param:"+param);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
@MyRequestMapping("/doTest2")
    public void test2(HttpServletRequest request,HttpServletResponse response){
        try {
            response.getWriter().println("doTest2 method success!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
