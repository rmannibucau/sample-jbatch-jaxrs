package com.github.rmannibucau.sample.generic;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.ItemProcessor;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Named
@Dependent
public class FutureDecoratorProcessor implements ItemProcessor {
    @Inject
    @BatchProperty
    private String ref;

    @Inject
    @BatchProperty
    private Boolean collection;

    private Function<Object, Object> delegate;

    @Override
    public Object processItem(final Object future) {
        if (delegate == null) {
            final ItemProcessor del = Locator.EE.find(ItemProcessor.class, ref);
            delegate = i -> {
                try {
                    return del.processItem(i);
                } catch (final Exception e) {
                    throw new IllegalArgumentException(e);
                }
            };
        }
        return CompletableFuture.class.cast(future)
                .thenApply(i -> collection ? Collection.class.cast(i).stream().map(delegate).collect(toList()) : delegate.apply(i));
    }
}
