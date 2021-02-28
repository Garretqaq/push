package com.regent.rpush.server.socket.client;

import com.regent.rpush.dto.message.NormalMessageDTO;

/**
 * 客户端
 *
 * @author 钟宝林
 * @since 2021/2/20/020 20:35
 **/
public interface RpushClient extends AutoCloseable {

    /**
     * 推送消息
     */
    void pushMessage(NormalMessageDTO message);
}
