package com.elasticsearch.cn.elasticsearch.result;


public class CommonResult<T> {

    private Integer code;
    private String msg;
    private T data;

    public CommonResult(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public CommonResult(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public CommonResult() {
        this.code = Status.SUCCESS.getCode();
        this.msg = Status.SUCCESS.getMessage();
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static CommonResult success(Status status) {
        return new CommonResult(status.getCode(), status.getMessage(), null);
    }

    public static CommonResult success(Integer code, String message) {
        return new CommonResult(code, message);
    }

    public static CommonResult success(Integer code, String message, Object obi) {
        return new CommonResult(code, message, obi);
    }

    public static CommonResult success(Status status, Object data) {
        return new CommonResult(status.getCode(), status.getMessage(), data);
    }

    public enum Status {
        SUCCESS(200, "success");

        private Integer code;
        private String message;

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        Status(Integer code, String message) {
            this.code = code;
            this.message = message;
        }
    }


}
