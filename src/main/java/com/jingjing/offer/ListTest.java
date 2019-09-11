package com.jingjing.offer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 对list边循环边删除和修改test
 * @Author: 020188
 * @Date: 2019/9/11
 */
public class ListTest {

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        for (int i=0 ;i<5;i++){
            list.add(i+"");
        }

        //OK
        int length = list.size();
        for (int i= 0;i<length;i++){
            if (list.get(i).equals("2")){
                list.add("2");
            }
        }

        //ConcurrentModificationException  并发异常

        //list中的modCount被修改了，但是iterator中的expectedCount没有修改，两个值不相等报并发异常
        for (String i:list) {
            if (i.equals("2")){
                list.add("2");
            }
        }

        //iterator没有add方法，这样还是回报并发异常
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()){
            String i = iterator.next();
            if (i.equals("2")){
                list.add("2");
            }
        }


        list.add(3,"2");
        //OK
        for (int i=list.size()-1;i>=0;i--){
            if (list.get(i).equals("2")){
                list.remove(i);
            }
        }

        //ConcurrentModificationException 并发异常，list.remove修改了modCount值未改变expectedCount值
        for (String i: list) {
            if (i.equals("2")){
                list.remove(i);
            }
        }

        //ok
        Iterator<String> iterator1 = list.iterator();
        while (iterator1.hasNext()){
            String i = iterator1.next();
            if (i.equals("2")){
                iterator1.remove();
            }
        }
        System.out.println(list);




    }
}
