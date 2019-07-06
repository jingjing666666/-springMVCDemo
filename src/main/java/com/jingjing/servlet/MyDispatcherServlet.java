package com.jingjing.servlet;

import com.jingjing.annotion.MyController;
import com.jingjing.annotion.MyRequestMapping;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @Author: 020188
 * @Date: 2019/7/3
 */
public class MyDispatcherServlet extends HttpServlet {

    private Properties properties = new Properties();

    private List<String> classNames = new ArrayList<>();

    private Map<String, Object> ioc = new HashMap<>();

    private Map<String, Method> handleMapping = new HashMap<>();

    private Map<String, Object> controllerMap = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            //处理请求
            doDispatch(req, resp);
        }catch (Exception e){
            resp.getWriter().write("500!! Server Exception");
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (handleMapping.isEmpty()){
            return;
        }

        String url = req.getRequestURI();

        String contextPath = req.getContextPath();

        url = url.replace(contextPath,"").replaceAll("/+","/");

        if (!this.handleMapping.containsKey(url)){
            resp.getWriter().write("404 NOT FOUND!");
            return;
        }

        Method method = handleMapping.get(url);

        //获取方法的参数类型列表
        Class<?>[] parameterTypes = method.getParameterTypes();


        //获取请求的参数
        Map<String,String[]> parameterMap = req.getParameterMap();

        //保存参数值
        Object[] paramValues = new Object[parameterTypes.length];

        //方法的参数列表
        for (int i = 0;i<parameterTypes.length;i++){
            //根据参数名称，做某些处理
            String requestParam = parameterTypes[i].getSimpleName();

            if (requestParam.equals("HttpServletRequest")){
                //参数类型已明确，强转类型
                paramValues[i] = req;
                continue;
            }

            if (requestParam.equals("HttpServletResponse")){
                paramValues[i] = resp;
                continue;
            }

            if (requestParam.equals("String")){
                for(Map.Entry<String,String[]> param: parameterMap.entrySet()){
                    String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]",
                            "").replaceAll(",\\s",",");
                    paramValues[i] = value;
                }

            }
        }

        //利用反射机制调用
        try {
            method.invoke(this.controllerMap.get(url),paramValues);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1.加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //2.初始化所有相关联的类
        doScanner(properties.getProperty("scanPackage"));

        //3.拿到扫描到的类，通过反射机制，实例化，并且放到ioc容器中(k-v beanName-bean) beanName默认首字母小写
        doInstance();

        //4.初始化HandleMapping（将url和method对应上）
        initHandlerMapping();

    }

    private void initHandlerMapping() {
        if (ioc.isEmpty()){
            return;
        }

        for (Map.Entry<String,Object> entry: ioc.entrySet()){
            Class<?extends Object> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(MyController.class)){
                continue;
            }

            //拼url时，是controller头上的URL拼上方法上的url
            String baseUrl = "";
            if (clazz.isAnnotationPresent(MyRequestMapping.class)){
                MyRequestMapping annotation = clazz.getAnnotation(MyRequestMapping.class);
                //baseUrl是类上的requestMapping的url地址
                baseUrl = annotation.value();

            }
            //获取类的所有方法
            Method[] methods = clazz.getMethods();
            for (Method method: methods) {
                if (!method.isAnnotationPresent(MyRequestMapping.class)){
                    continue;
                }

                MyRequestMapping annotation = method.getAnnotation(MyRequestMapping.class);
                String url = annotation.value();

                url =(baseUrl+"/"+url).replaceAll("/+","/");
                handleMapping.put(url,method);
                try {
                    controllerMap.put(url,clazz.newInstance());
                    System.out.println(url + "," + method);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }
    }

    private void doInstance() {
        if (classNames.isEmpty()){
            return;
        }
        for (String className : classNames) {

            //把类找出来，通过反射来实例化（只有加@MyController需要实例化）
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyController.class)){
                    ioc.put(toLowerFirsrWord(clazz.getSimpleName()),clazz.newInstance());
                }else {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    /**
     * 把字符串的首字母小写
     * @param name
     * @return
     */

    private String toLowerFirsrWord(String name) {
        char[] charArray = name.toCharArray();
        charArray[0]+= 32;
        return String.valueOf(charArray);

    }

    private void doScanner(String scanPackage) {
        //把所有的.替换成/
        URL url = this.getClass().getClassLoader().getResource("/"+scanPackage.replaceAll("\\.","/"));

        File dir = new File(url.getFile());
        for (File file :dir.listFiles()){
            if (file.isDirectory()){
                //递归读取包
                doScanner(scanPackage+"."+file.getName());
            }else {
                String className = scanPackage+"."+file.getName().replace(".class","");
                classNames.add(className);
            }
        }
    }

    private void doLoadConfig(String contextConfigLocation) {
        System.out.println("1");
        //把web.xml中的contextConfigLocation对应value值的文件加载到流里面
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        //用properties文件加载文件里的内容
        System.out.println("2");
        try {
            properties.load(resourceAsStream);
            System.out.println("3");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //关流
            if (null!=resourceAsStream){
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
