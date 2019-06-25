package com.pinyougou.entity;

import java.io.Serializable;

/**
 * 返回结果封装对象
 */
public class ResultInfo  implements Serializable {
    //操作是否成功
    private boolean success;
    //操作返回的信息
    private String message;

    public ResultInfo(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ResultInfo{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
