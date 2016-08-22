package com.github.rmannibucau.sample.generic;

import lombok.NoArgsConstructor;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class Locator {
    public static final class EE {  // for normal scoped beans

        public static <T> T find(final Class<T> type, final String name) {
            final BeanManager beanManager = CDI.current().getBeanManager();
            return type.cast(
                    beanManager.getReference(beanManager.resolve(beanManager.getBeans(name)), type, null));
        }
    }
}
