package com.doc.search.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    /**
     * 成功
     */
    public static final int OK = 0;
    /**
     * 失败
     */
    public static final int FAIL = 1;

    private Integer code;
    private String msg;

    private T data;

    public static <T> Result<T> success(T data){
        return new Result<>(OK,"OK",data);
    }


    public static <T> Result<T> success(){
        return Result.<T>builder()
                .code(OK)
                .msg("OK")
                .build();
    }

    public static <T> Result<T> fail(){
        return Result.<T>builder()
                .code(FAIL)
                .msg("FAIL")
                .build();
    }

}
