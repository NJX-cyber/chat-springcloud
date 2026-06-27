package com.chat.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * author:normal
 * date:2026/1/5 16:38
 * description:
 */
public class CopyUtils {
    public static <T, S> List<T> copyList(List<S> tList, Class<T> tClass) {
        List<T> list = new ArrayList<>();
        for (S s : tList) {
            T t = null;
            try {
                t = tClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            BeanUtils.copyProperties(s, t);
            list.add(t);
        }
        return list;
    }

    public static <T, S> T copy(S s, Class<T> tClass) {
        T t = null;
        try {
            t = tClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        BeanUtils.copyProperties(s, t);
        return t;
    }
}