package fi.jubic.quanta.auth;

import fi.jubic.easyschedule.Task;
import fi.jubic.quanta.config.Configuration;
import fi.jubic.quanta.dao.UserDao;
import fi.jubic.quanta.exception.ApplicationException;
import fi.jubic.quanta.models.User;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class AdminAuthenticationTask implements Task {
    private final Configuration configuration;
    private final UserDao userDao;

    @Inject
    AdminAuthenticationTask(
            Configuration configuration,
            UserDao userDao
    ) {
        this.configuration = configuration;
        this.userDao = userDao;
    }

    public void run() {
        if (configuration.getAdmin().getUsername().length() == 0
                || configuration.getAdmin().getPassword().length() == 0
        ) {
            return;
        }
        Admin admin = configuration.getAdmin();

        Optional<User> adminUser = userDao.getUserByName(
                admin.getUsername()
        );

        if (!adminUser.isPresent()) {
            userDao.createAdmin(
                    admin
            ).orElseThrow(
                    () -> new ApplicationException("Couldn't create Admin account")
            );
        }
    }

}
