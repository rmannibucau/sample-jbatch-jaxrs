package com.github.rmannibucau.sample;

import com.github.rmannibucau.sample.model.Login;
import org.apache.batchee.extras.typed.TypedItemWriter;

import javax.enterprise.context.Dependent;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static java.util.Optional.ofNullable;

@Named
@Dependent
public class LoginWriter extends TypedItemWriter<Login, Long> {
    @PersistenceContext
    private EntityManager em;

    @Override
    protected void doWriteItems(final List<Login> list) {
        list.forEach(l -> em.persist(l));
    }

    @Override
    protected void doOpen(final Long aLong) {
        ofNullable(aLong).ifPresent(checkpoint -> {
            // TODO: restore
        });
    }

    @Override
    protected Long doCheckpointInfo() {
        return null;  // for now we don't support restart and just replay
    }

    @Override
    public void close() throws Exception {
        // no-op thanks to JTA
    }
}
