package com.vcg.search.bean;

import lombok.Data;

/**
 * Created by dongsijia on 2019/2/14.
 */
@Data public class Render {

    private int code;

    private String msg;

    public Render() {
    }

    public Render(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
