package com.regent.rpush.dto.message;

import com.regent.rpush.dto.message.base.BaseMessage;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 企业微信消息发送DTO
 *
 * @author 钟宝林
 * @since 2021/2/28/028 21:28
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class WechatWorkMessageDTO extends BaseMessage {
    private static final long serialVersionUID = -3289428483627765265L;

    private String content;

}
