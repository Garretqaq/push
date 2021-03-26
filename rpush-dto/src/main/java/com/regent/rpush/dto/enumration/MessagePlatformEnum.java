package com.regent.rpush.dto.enumration;

import cn.hutool.core.lang.Assert;
import com.regent.rpush.dto.message.config.Config;
import com.regent.rpush.dto.message.config.EmailConfig;
import com.regent.rpush.dto.message.config.EmptyConfig;
import com.regent.rpush.dto.message.config.WechatWorkConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 消息平台枚举
 *
 * @author 钟宝林
 * @date 2021/2/8 9:36
 **/
public enum MessagePlatformEnum {

    EMAIL(EmailConfig.class, "邮箱", "", "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,})$", true),
    WECHAT_WORK(WechatWorkConfig.class, "企业微信", "", "", true),
    //    DING_TALK("钉钉", "", true),
    RPUSH_SERVER(EmptyConfig.class, "rpush服务", "", "", false)
    ;

    private final String name;
    private final String description;
    /**
     * 校验用的正则表达式
     */
    private final String validateReg;
    private final boolean enable;
    /**
     * 配置类型
     */
    private Class<? extends Config> configType;

    MessagePlatformEnum(Class<? extends Config> configType, String name, String description, String validateReg, boolean enable) {
        this.name = name;
        this.description = description;
        this.validateReg = validateReg;
        this.enable = enable;
        this.configType = configType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnable() {
        return enable;
    }

    public String getValidateReg() {
        return validateReg;
    }

    public Class<? extends Config> getConfigType() {
        return configType;
    }

    public boolean matcher(String testStr) {
        if (StringUtils.isBlank(testStr) || StringUtils.isBlank(validateReg)) {
            return true;
        }
        Pattern pattern = Pattern.compile(validateReg);
        Matcher matcher = pattern.matcher(testStr);
        return matcher.matches();
    }

    public void matcherThrow(String testStr) {
        Assert.isTrue(matcher(testStr), testStr + "格式不符，请检查后重试");
    }
}
