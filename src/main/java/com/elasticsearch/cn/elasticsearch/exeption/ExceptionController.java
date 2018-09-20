/*
package com.elasticsearch.cn.elasticsearch.exeption;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.elasticsearch.cn.elasticsearch.result.CommonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;


@Controller
public class ExceptionController implements ErrorController {

    private static final String ERROR_PAGE_PATH = "error";

    private ErrorAttributes errorAttributes;

    @Override
    public String getErrorPath() {
        return ERROR_PAGE_PATH;
    }

    @Autowired
    public ExceptionController (ErrorAttributes errorAttributes){
        this.errorAttributes=errorAttributes;
    }

    @RequestMapping(value = ERROR_PAGE_PATH,produces = "text/html")
    public String errorPageExeptionHandler(HttpServletRequest request, HttpServletResponse response){
        int responseCode = response.getStatus();
        switch (responseCode){
            case 403:
                return "403";
            case 404:
                return "404";
            case 500:
                return "500";
        }
        return "index";
    }

    */
/**
     * 处理json之类的异常
     *//*

    @RequestMapping(value = ERROR_PAGE_PATH)
    @ResponseBody
    public CommonResult errorHandler(HttpServletRequest request) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);

        Map<String, Object> attr = this.errorAttributes.getErrorAttributes(requestAttributes, false);
        int status = getStatus(request);

        return CommonResult.success(status, String.valueOf(attr.getOrDefault("message", "error")));
    }

    private int getStatus(HttpServletRequest request) {
        Integer status = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (status != null) {
            return status;
        }
        return 500;
    }

}
*/
