package com.yijiang.io.rpc;

import java.io.Serializable;

/**
 * @Auther: jiangyi
 * @Date: 2022-02-04
 * @Description: 调用远程接口的服务，方法，参数
 */
public class MsgBody implements Serializable{


    private static final long serialVersionUID = 1372811528016065248L;
    private String interfaceName;
    private Class<?>[] parameterTypes;
    private String methodName;
    private Object[] args;
    private String responseString;

    public MsgBody() {
    }

    public String getResponseString() {
        return responseString;
    }

    public void setResponseString(String responseString) {
        this.responseString = responseString;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
