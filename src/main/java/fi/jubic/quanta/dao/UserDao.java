package fi.jubic.quanta.dao;

import fi.jubic.quanta.auth.Admin;
import fi.jubic.quanta.db.tables.records.UserRecord;
import fi.jubic.quanta.models.User;
import fi.jubic.quanta.util.HashUtil;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.Optional;

import static fi.jubic.quanta.db.Tables.USER;

@Singleton
public class UserDao {
    private final org.jooq.Configuration conf;

    @Inject
    UserDao(fi.jubic.quanta.config.Configuration configuration) {
        this.conf = configuration.getJooqConfiguration().getConfiguration();
    }

    public Optional<User> getUserByName(String name) {
        return getBy(USER.NAME.eq(name), conf);
    }

    public Optional<User> createAdmin(Admin admin) {
        return createAdmin(admin, conf);
    }

    private Optional<User> createAdmin(
            Admin admin,
            Configuration transaction
    ) {
        String salt = HashUtil.generateSalt();
        User user = User.builder()
                .setId(-1L)
                .setName(admin.getUsername())
                .setRole("ADMIN")
                .setSalt(salt)
                .setPasswordHash(
                        HashUtil.hash(
                                admin.getPassword(),
                                salt
                        )
                )
                .setCreationDate(Instant.now())
                .build();

        UserRecord record = User.mapper.write(
                DSL.using(transaction).newRecord(USER),
                user
        );
        record.store();

        return getUserByName(
                record.getName()
        );
    }

    private Optional<User> getBy(Condition condition, Configuration transaction) {
        return DSL.using(transaction)
                .select()
                .from(USER)
                .where(condition)
                .fetchOptional()
                .map(User.mapper::map);

    }
}
