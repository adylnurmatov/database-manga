package org.adyl.utils;

import org.springframework.beans.BeansException;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContext implements org.springframework.context.ApplicationContextAware {
    private static org.springframework.context.ApplicationContext context;
    @Override
    public void setApplicationContext(org.springframework.context.ApplicationContext applicationContext) throws BeansException {
        ApplicationContext.context = applicationContext;
    }

    public static org.springframework.context.ApplicationContext getContext() {
        return context;
    }
}
