package com.chat.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.chat.enums.ResponseCodeEnum;
import com.chat.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * author:normal
 * date:2026/1/26 22:13
 * description:
 */
public class JsonUtils {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    public static SerializerFeature[] FEATURES = new SerializerFeature[]{SerializerFeature.WriteMapNullValue};

    public static String convertObj2Json(Object o) {
        return JSON.toJSONString(o, FEATURES);
    }

    public static <T> T convertJson2Obj(String json, Class<T> tClass) {
        try {
            return JSONObject.parseObject(json, tClass);
        } catch (Exception e) {
            logger.error("convertJson2ObjError,json{}", json);
            throw new BusinessException(ResponseCodeEnum.CODE_601);
        }
    }

    public static <T> List<T> convertJsonArray2List(String json, Class<T> tClass) {
        try {
            return JSONObject.parseArray(json, tClass);
        } catch (Exception e) {
            logger.error("convertJsonArray2ListError,json{}", json);
            throw new BusinessException(ResponseCodeEnum.CODE_601);
        }
    }
}