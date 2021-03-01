package fi.jubic.quanta.controller;

import fi.jubic.quanta.dao.UserDao;
import fi.jubic.quanta.models.User;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class UserController {
    private final UserDao userDao;

    @Inject
    UserController(
            UserDao userDao
    ) {
        this.userDao = userDao;
    }

    public Optional<User> getUsers() {
        return userDao.getUsers();
    }

    public Optional<User> getUserByName(String name) {
        return userDao.getUserByName(name);
    }

}
