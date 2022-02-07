package com.yijiang.io.rpc;

/**
 * @Auther: jiangyi
 * @Date: 2022-02-04
 * @Description: com.yijiang.io.rpc
 */
public class CarImpl implements Car{
    @Override
    public String drive(String brand) {
        return brand;
    }
}
