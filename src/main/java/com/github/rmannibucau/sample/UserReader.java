package com.github.rmannibucau.sample;

import com.github.rmannibucau.sample.model.User;
import org.apache.batchee.extras.typed.TypedItemReader;

import javax.annotation.Resource;
import javax.batch.api.BatchProperty;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

// Note: this doesn't respect JTA on that side, this is doable with some more effort
//
// main point here is to create a CompletionFuture of Collection<User>
//
@Named
@Dependent
public class UserReader extends TypedItemReader<CompletableFuture<Collection<User>>, Long> {
    private static final GenericType<Collection<User>> USER_COLLECTION = new GenericType<Collection<User>>() {
    };

    @Resource
    private ManagedExecutorService mes;

    @Inject
    @BatchProperty
    private String base;

    private Client client;
    private WebTarget target;
    private int from = 0;

    @Override
    protected void doOpen(final Long aLong) {
        ofNullable(aLong).ifPresent(checkpoint -> {
            // TODO: restore
        });
        client = ClientBuilder.newClient();
        target = client.target(base).path("users");
    }

    @Override
    protected CompletableFuture<Collection<User>> doRead() {
        if (from > 10) {
            return null;
        }

        final Invocation.Builder builder = target.queryParam("from", this.from).request(APPLICATION_JSON_TYPE);
        from += 5; // see the endpoint, in real life we use from + pageSize but this is simplified for the sample

        final CompletableFuture<Collection<User>> future = new CompletableFuture<>();
        builder.async().get(new InvocationCallback<Collection<User>>() {
            @Override
            public void completed(final Collection<User> users) {
                future.complete(users);
            }

            @Override
            public void failed(final Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future;
    }

    @Override
    public void close() throws Exception {
        client.close();
    }

    @Override
    protected Long doCheckpointInfo() {
        return null; // for now we don't support restart
    }
}
