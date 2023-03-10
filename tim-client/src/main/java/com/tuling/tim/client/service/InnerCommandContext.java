package com.tuling.tim.client.service;

import com.tuling.tim.client.service.impl.command.PrintAllCommand;
import com.tuling.tim.client.util.SpringBeanFactory;
import com.tuling.tim.common.enums.SystemCommandEnum;
import com.tuling.tim.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @since JDK 1.8
 */
@Component
public class InnerCommandContext {
    private final static Logger LOGGER = LoggerFactory.getLogger(InnerCommandContext.class);

    /**
     * 获取执行器实例
     *  通过反射获得需要内部指令执行实例类
     * @param command 执行器实例
     * @return
     */
    public InnerCommand getInstance(String command) {

        Map<String, String> allClazz = SystemCommandEnum.getAllClazz();

        //兼容需要命令后接参数的数据 :q tuling
        String[] trim = command.trim().split(" ");
        String clazz = allClazz.get(trim[0]);
        InnerCommand innerCommand = null;
        try {
            if (StringUtil.isEmpty(clazz)) {
                clazz = PrintAllCommand.class.getName();
            }
            innerCommand = (InnerCommand) SpringBeanFactory.getBean(Class.forName(clazz));
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }

        return innerCommand;
    }

}
