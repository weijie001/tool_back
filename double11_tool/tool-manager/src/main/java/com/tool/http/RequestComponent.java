package com.tool.http;

import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;

import java.lang.reflect.Method;

public class RequestComponent {
    public String prefixUrl;
    public Object req;
    public RequestComponent(Object req,String env) {
        this.req = req;
        this.prefixUrl = env;
    }
    public  String exec(String path,Class<?> reqClass) throws Exception {
        String result = null;
        if (req != null) {
            Class<?> aClass = req.getClass();
            Method toByteArray = getMethod(aClass, "toByteArray", null);
            byte[] invoke = (byte[]) toByteArray.invoke(req);
            byte[] bytesOutPut = OkhttpUtils.sendData(prefixUrl + path, invoke);
            Method resultMethod = getMethod(reqClass, "parseFrom", new Class[]{byte[].class});
            Object obj = resultMethod.invoke(reqClass, bytesOutPut);
            result = JsonFormat.printToString((Message) obj);
            //System.out.println(result);
        } else {
            byte[] bytesOutPut = OkhttpUtils.sendData(prefixUrl + path, null);
            Method resultMethod = getMethod(reqClass, "parseFrom", new Class[]{byte[].class});
            Object obj = resultMethod.invoke(reqClass, bytesOutPut);
            result = JsonFormat.printToString((Message) obj);
            //System.out.println(result);
        }
        return result;
    }

    public static Method getMethod(Class clazz, String methodName, final Class[] classes) throws Exception {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName, classes);
        } catch (NoSuchMethodException e) {
            try {
                method = clazz.getMethod(methodName, classes);
            } catch (NoSuchMethodException ex) {
                if (clazz.getSuperclass() == null) {
                    return method;
                } else {
                    method = getMethod(clazz.getSuperclass(), methodName,classes);
                }
            }
        }
        return method;
    }
}
