package com.github.rmannibucau.sample;

import com.github.rmannibucau.sample.model.Login;
import com.github.rmannibucau.sample.model.User;
import org.apache.openejb.api.configuration.PersistenceUnitDefinition;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Default;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.RandomPort;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.batch.operations.JobOperator;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.net.URL;
import java.util.Collection;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static javax.batch.runtime.BatchRuntime.getJobOperator;
import static javax.batch.runtime.BatchStatus.COMPLETED;
import static org.apache.batchee.util.Batches.waitFor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Default
@Classes(cdi = true, context = "app", innerClassesAsBean = true)
@EnableServices("jaxrs")
@PersistenceUnitDefinition(properties = {
    "openjpa.jdbc.SynchronizeMappings=buildSchema(ForeignKeys=true)"
})
@RunWith(ApplicationComposer.class)
public class BatchTest {
    @RandomPort("http")
    private URL root;

    @PersistenceContext
    private EntityManager em;

    @Inject
    private Jta jta;

    @Test
    public void run() {
        jta.run(() -> em.createQuery("delete from Login").executeUpdate());

        final JobOperator operator = getJobOperator();
        assertEquals(COMPLETED, waitFor(operator, operator.start("sample", new PropertiesBuilder()
                .p("base", root.toExternalForm() + "app")
                .build())));

        final Collection<Login> logins = em.createQuery("select e from Login e order by e.username", Login.class).getResultList();
        assertEquals(15, logins.size());
        logins.forEach(l -> {
            assertNotNull(l.getTempPassword());
            assertNotNull(l.getUsername());
            assertTrue(l.getUsername().startsWith("user #"));
        });
    }

    @Path("users")
    @ApplicationScoped
    public static class UserEndpointGenerator {
        @GET
        public Collection<User> find(@QueryParam("from") final int from) {
            return IntStream.range(from, from + 5).mapToObj(i -> new User("user #" + i)).collect(toList());
        }
    }

    @ApplicationScoped
    public static class Jta {
        @Transactional
        public void run(final Runnable task) {
            task.run();
        }
    }
}
