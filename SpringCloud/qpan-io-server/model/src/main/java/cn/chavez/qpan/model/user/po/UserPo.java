package cn.chavez.qpan.model.user.po;


import cn.chavez.qpan.model.BassPo;
import lombok.Data;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/2/3 15:43
 */
@Data
public class UserPo extends BassPo {
  private String phoneNumber;
  private String account;
  private String nickName;
  private String password;
  private String dirAddress;
  private long personalTotalByte;
  private long personalFreeByte;
}
