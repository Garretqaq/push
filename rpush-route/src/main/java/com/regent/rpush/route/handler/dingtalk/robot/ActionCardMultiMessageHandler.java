package com.regent.rpush.route.handler.dingtalk.robot;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.regent.rpush.common.RpushUtils;
import com.regent.rpush.dto.enumration.MessageType;
import com.regent.rpush.dto.message.config.DingTalkRobotConfig;
import com.regent.rpush.dto.message.dingtalk.robot.ActionCardMultiMessageDTO;
import com.regent.rpush.dto.message.dingtalk.robot.BtnJsonDTO;
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
 * 钉钉群机器人ACTION_CARD_MULTI类型消息处理器
 *
 * @author 钟宝林
 **/
@Component
public class ActionCardMultiMessageHandler extends MessageHandler<ActionCardMultiMessageDTO> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ActionCardMultiMessageHandler.class);

    @Autowired
    private IRpushTemplateReceiverGroupService rpushTemplateReceiverGroupService;

    @Override
    public MessageType messageType() {
        return MessageType.DING_TALK_ROBOT_ACTION_CARD_MULTI;
    }

    @Override
    public void handle(ActionCardMultiMessageDTO param) {
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
                request.setMsgtype("actionCard");
                OapiRobotSendRequest.Actioncard actioncard = new OapiRobotSendRequest.Actioncard();
                actioncard.setTitle(param.getTitle());
                actioncard.setText(param.getText());
                actioncard.setBtnOrientation(param.getBtnOrientation());
                List<OapiRobotSendRequest.Btns> btns = new ArrayList<>();
                for (BtnJsonDTO btnJsonDTO : param.getBtns()) {
                    OapiRobotSendRequest.Btns btnJson = new OapiRobotSendRequest.Btns();
                    btnJson.setActionURL(btnJsonDTO.getActionURL());
                    btnJson.setTitle(btnJsonDTO.getTitle());
                    btns.add(btnJson);
                }
                actioncard.setBtns(btns);
                request.setActionCard(actioncard);
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
