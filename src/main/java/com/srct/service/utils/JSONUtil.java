package com.srct.service.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JSONUtil {

    private JSONUtil() {

    }

    /**
     * Transfer object to JSON string
     *
     * @param object
     * @return
     */
    public static String toJSONString(Object object) {
        return generateJSONString(object, false, false, false);
    }

    public static String toJSONString(Object object, boolean needOrder, boolean ignoreEmpty, boolean ignoreIndent) {
        return generateJSONString(object, needOrder, ignoreEmpty, ignoreIndent);
    }

    private static String generateJSONString(Object object, boolean needOrder, boolean ignoreEmpty,
            boolean ignoreIndent) {
        String result = null;
        ObjectMapper objectMapper = new ObjectMapper();
        // set config of JSON
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);// can use single quote
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);// allow unquoted field names
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, needOrder); // ASCII order
        if (ignoreEmpty) {
            objectMapper.setSerializationInclusion(Include.NON_EMPTY);
        }
        if (!ignoreIndent) {
            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, Boolean.TRUE);
        }
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));// set date format

        try {
            result = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            //Log.e("Generate JSON String error! %s", e.getMessage());
        }
        return result;
    }
    
    public static String makeJSONMap(Object object) {
        String result = null;
        ObjectMapper objectMapper = new ObjectMapper();
        // set config of JSON
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);// can use single quote
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);// allow unquoted field names
        objectMapper.configure(JsonParser.Feature.ALLOW_MISSING_VALUES, true);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));// set date format
        try {
            result = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            //Log.e("Generate JSON String error! %s", e.getMessage());
        }
        return result;
    }

    /**
     * 获取泛型的Collection Type
     * 
     * @param jsonStr
     *            json字符串
     * @param collectionClass
     *            泛型的Collection
     * @param elementClasses
     *            元素类型
     */

    // YourBean bean = (YourBean)readJson(jsonString, YourBean.class)
    // List<YourBean> list = (List<YourBean>)readJson(jsonString, List.class,
    // yourBean.class);
    // Map<H,D> map = (Map<H,D>)readJson(jsonString, HashMap.class, String.class,
    // YourBean.class);
    public static <T> T readJson(String jsonStr, Class<?> collectionClass, Class<?>... elementClasses)
            throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JavaType javaType = mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);

        return mapper.readValue(jsonStr, javaType);

    }

    public static boolean isJSONValid(String jsonInString) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(jsonInString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
