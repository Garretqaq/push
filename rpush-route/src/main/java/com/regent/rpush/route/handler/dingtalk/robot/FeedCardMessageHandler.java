package com.regent.rpush.route.handler.dingtalk.robot;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.regent.rpush.common.RpushUtils;
import com.regent.rpush.dto.enumration.MessageType;
import com.regent.rpush.dto.message.config.DingTalkRobotConfig;
import com.regent.rpush.dto.message.dingtalk.robot.FeedCardMessageDTO;
import com.regent.rpush.route.handler.MessageHandler;
import com.regent.rpush.route.model.RpushMessageHisDetail;
import com.regent.rpush.route.service.IRpushTemplateReceiverGroupService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 钉钉群机器人ACTION_CARD_SINGLE类型消息处理器
 *
 * @author 钟宝林
 **/
@Component
public class FeedCardMessageHandler extends MessageHandler<FeedCardMessageDTO> {

    private final static Logger LOGGER = LoggerFactory.getLogger(FeedCardMessageHandler.class);

    @Autowired
    private IRpushTemplateReceiverGroupService rpushTemplateReceiverGroupService;

    @Override
    public MessageType messageType() {
        return MessageType.DING_TALK_ROBOT_FEED_CARD;
    }

    @Override
    public void handle(FeedCardMessageDTO param) {
        List<DingTalkRobotConfig> configs = rpushPlatformConfigService.queryConfigOrDefault(param, DingTalkRobotConfig.class, messageType().getPlatform());
        for (DingTalkRobotConfig config : configs) {
            Set<String> receiverUsers = rpushTemplateReceiverGroupService.listReceiverIds(param.getReceiverGroupIds(), param.getClientId()); // 先拿参数里分组的接收人
            if (param.getReceiverIds() != null) {
                receiverUsers.addAll(param.getReceiverIds());
            }

            if (receiverUsers.size() <= 0) {
                LOGGER.warn("请求号：{}，消息配置：{}。没有检测到接收用户", param.getRequestNo(), config.getConfigName());
                return;
            }

            RpushMessageHisDetail hisDetail = RpushMessageHisDetail.builder()
                    .platform(messageType().getPlatform().name())
                    .messageType(messageType().name())
                    .configName(config.getConfigName())
                    .receiverId(StringUtils.join(receiverUsers))
                    .requestNo(param.getRequestNo())
                    .configId(config.getConfigId())
                    .build();
            try {
                DingTalkClient client = new DefaultDingTalkClient(config.getWebhook());
                OapiRobotSendRequest request = new OapiRobotSendRequest();
                request.setMsgtype("feedCard");
                OapiRobotSendRequest.Feedcard feedcard = new OapiRobotSendRequest.Feedcard();
                List<FeedCardMessageDTO.Item> items = param.getItems();
                List<OapiRobotSendRequest.Links> links = new ArrayList<>();
                for (FeedCardMessageDTO.Item item : items) {
                    OapiRobotSendRequest.Links link = new OapiRobotSendRequest.Links();
                    link.setTitle(item.getTitle());
                    link.setMessageURL(item.getMessageURL());
                    link.setPicURL(item.getPicURL());
                    links.add(link);
                }
                feedcard.setLinks(links);
                request.setFeedCard(feedcard);
                OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
                at.setIsAtAll(param.isAtAll());
                at.setAtMobiles(RpushUtils.getMobile(receiverUsers));
                at.setAtUserIds(RpushUtils.getNotMobile(receiverUsers));
                request.setAt(at);
                OapiRobotSendResponse rsp = client.execute(request);
                if (!rsp.isSuccess()) {
                    throw new IllegalStateException(rsp.getBody());
                }
                hisDetail.setSendStatus(RpushMessageHisDetail.SEND_STATUS_SUCCESS);
                hisDetail.setErrorMsg(rsp.getBody());
            } catch (Exception e) {
                String eMessage = ExceptionUtil.getMessage(e);
                eMessage = StringUtils.isBlank(eMessage) ? "未知错误" : eMessage;
                hisDetail.setSendStatus(RpushMessageHisDetail.SEND_STATUS_FAIL);
                hisDetail.setErrorMsg(eMessage);
            }
            rpushMessageHisService.logDetail(param.getClientId(), hisDetail);
        }
    }
}
