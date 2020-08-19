package cn.chavez.qpan.exception;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/2/8 19:13
 */
public class UserAccountNotFoundException extends Exception{

    public UserAccountNotFoundException(String msg){
        super(msg);
    }
}
