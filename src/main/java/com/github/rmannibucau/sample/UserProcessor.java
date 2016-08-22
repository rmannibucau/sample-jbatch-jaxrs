package com.github.rmannibucau.sample;

import com.github.rmannibucau.sample.model.Login;
import com.github.rmannibucau.sample.model.User;
import org.apache.batchee.extras.typed.TypedItemProcessor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.UUID;

@Named
@ApplicationScoped
public class UserProcessor extends TypedItemProcessor<User, Login> {
    @Override
    protected Login doProcessItem(final User user) {
        return new Login(user.getName(), UUID.randomUUID().toString());
    }
}
