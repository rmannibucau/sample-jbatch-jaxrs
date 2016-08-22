package com.github.rmannibucau.sample.generic;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.ItemWriter;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;

@Named
@Dependent
public class FlatteningCollectionWriter implements ItemWriter {
    @Inject
    @BatchProperty
    private String ref;

    @Inject
    @BatchProperty
    private String waitTimeout;

    private ItemWriter delegate;
    private long timeout;

    @Override
    public void open(final Serializable checkpoint) throws Exception {
        delegate = Locator.EE.find(ItemWriter.class, ref);
        delegate.open(checkpoint);
        timeout = waitTimeout == null ? -1 : Long.parseLong(waitTimeout);
    }

    @Override
    public void close() throws Exception {
        delegate.close();
    }

    @Override
    public void writeItems(final List<Object> items) throws Exception {
        delegate.writeItems(items.stream()
                .map(e -> {
                    if (CompletableFuture.class.isInstance(e) && timeout > 0) {
                        try {
                            return CompletableFuture.class.cast(e).get(timeout, MILLISECONDS);
                        } catch (final InterruptedException e1) {
                            Thread.interrupted();
                            throw new IllegalStateException(e1);
                        } catch (final ExecutionException e1) {
                            throw new IllegalStateException(e1.getCause());
                        } catch (final TimeoutException e1) {
                            throw new IllegalStateException(e1);
                        }
                    }
                    return e;
                })
                .flatMap(c -> {
                    final Collection<Object> collection = Collection.class.cast(c); // just to "type" it enough for the stream API
                    return collection.stream();
                })
                .collect(toList()));
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        return delegate.checkpointInfo();
    }
}
