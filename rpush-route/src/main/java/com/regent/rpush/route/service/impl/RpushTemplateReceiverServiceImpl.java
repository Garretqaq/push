package com.regent.rpush.route.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.regent.rpush.dto.enumration.MessagePlatformEnum;
import com.regent.rpush.dto.message.base.BaseMessage;
import com.regent.rpush.route.mapper.RpushTemplateReceiverMapper;
import com.regent.rpush.route.model.RpushTemplateReceiver;
import com.regent.rpush.route.service.IRpushTemplateReceiverService;
import com.regent.rpush.route.utils.Qw;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 消息模板-预设接收人表 服务实现类
 * </p>
 *
 * @author 钟宝林
 * @since 2021-03-06
 */
@Service
public class RpushTemplateReceiverServiceImpl extends ServiceImpl<RpushTemplateReceiverMapper, RpushTemplateReceiver> implements IRpushTemplateReceiverService {

    @Override
    public void updateReceiver(RpushTemplateReceiver receiver) {
        String receiverId = receiver.getReceiverId();
        Long id = receiver.getId();
        MessagePlatformEnum platform = MessagePlatformEnum.valueOf(receiver.getPlatform());
        platform.matcherThrow(receiverId); // 验证格式
        if (StringUtils.isNotBlank(receiverId)) {
            // id判重
            QueryWrapper<RpushTemplateReceiver> receiverNameQw = Qw.newInstance(RpushTemplateReceiver.class)
                    .eq("group_id", receiver.getGroupId())
                    .eq("receiver_id", receiverId);
            if (id != null) {
                receiverNameQw.ne("id", id);
            }
            RpushTemplateReceiver existReceiver = getOne(receiverNameQw);
            Assert.isTrue(existReceiver == null, "接收人重复");
        }

        // 入库
        if (id == null) {
            save(receiver);
        } else {
            updateById(receiver);
        }
    }

    @Override
    public Set<String> parseReceiver(BaseMessage param) {
        Set<String> receiverIds = new HashSet<>();
        if (CollUtil.isNotEmpty(param.getGroupIds())) {
            List<RpushTemplateReceiver> receivers = list(Qw.newInstance(RpushTemplateReceiver.class).in("group_id", param.getGroupIds()));
            if (receivers != null && receivers.size() > 0) {
                receiverIds.addAll(receivers.stream().map(RpushTemplateReceiver::getReceiverId).collect(Collectors.toList()));
            }
        }
        if (CollUtil.isNotEmpty(param.getSendTos())) {
            receiverIds.addAll(param.getSendTos());
        }
        return receiverIds;
    }
}
