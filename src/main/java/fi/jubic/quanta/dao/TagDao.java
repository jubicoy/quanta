package fi.jubic.quanta.dao;

import fi.jubic.quanta.models.Tag;
import org.jooq.Record;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static fi.jubic.quanta.db.Tables.TAG;
import static fi.jubic.quanta.db.Tables.TAG_DATACONNECTION;
import static fi.jubic.quanta.db.Tables.TAG_TASK;
import static org.jooq.impl.DSL.select;


@Singleton
public class TagDao {
    private final org.jooq.Configuration conf;

    @Inject
    TagDao(fi.jubic.quanta.config.Configuration conf) {
        this.conf = conf.getJooqConfiguration().getConfiguration();
    }

    public List<Tag> getAllTaskTags() {
        return DSL.using(conf)
                .select()
                .from(TAG)
                .where(TAG.ID.in(select(TAG_TASK.TAG_ID).from(TAG_TASK)))
                .fetchStream()
                .collect(Tag.mapper);

    }

    public List<Tag> getAllDataConnectionTags() {
        return DSL.using(conf)
                .select()
                .from(TAG)
                .where(TAG.ID.in(select(TAG_DATACONNECTION.TAG_ID).from(TAG_DATACONNECTION)))
                .fetchStream()
                .collect(Tag.mapper);

    }

    public List<Long> searchDataConnections(List<Long> tagIds) {
        return DSL.using(conf)
                .select(TAG_DATACONNECTION.DATACONNECTION_ID)
                .from(TAG_DATACONNECTION)
                .where(TAG_DATACONNECTION.TAG_ID.in(tagIds))
                .groupBy(TAG_DATACONNECTION.DATACONNECTION_ID)
                .having(DSL.count(TAG_DATACONNECTION.TAG_ID).eq(tagIds.size()))
                .fetch()
                .getValues(TAG_DATACONNECTION.DATACONNECTION_ID);
    }

    public List<Long> searchTasks(List<Long> tagIds) {
        return DSL.using(conf)
                .select(TAG_TASK.TASK_ID)
                .from(TAG_TASK)
                .where(TAG_TASK.TAG_ID.in(tagIds))
                .groupBy(TAG_TASK.TASK_ID)
                .having(DSL.count(TAG_TASK.TAG_ID).eq(tagIds.size()))
                .fetch()
                .getValues(TAG_TASK.TASK_ID);
    }

    public List<Tag> getDataConnectionTags(Long dataConnectionId) {
        return DSL.using(conf)
                .select()
                .from(TAG)
                .leftJoin(TAG_DATACONNECTION)
                .on(TAG.ID.eq(TAG_DATACONNECTION.TAG_ID))
                .where(TAG_DATACONNECTION.DATACONNECTION_ID.eq(dataConnectionId))
                .fetchStream()
                .collect(Tag.mapper);
    }

    public List<Tag> getTaskTags(Long taskId) {
        return DSL.using(conf)
                .select()
                .from(TAG)
                .leftJoin(TAG_TASK)
                .on(TAG.ID.eq(TAG_TASK.TAG_ID))
                .where(TAG_TASK.TASK_ID.eq(taskId))
                .fetchStream()
                .collect(Tag.mapper);
    }

    private void addTagsToTask(Long taskId, Set<String> tagNames) {
        tagNames.forEach((name) -> {
            Record record = DSL.using(conf)
                    .select(TAG.ID).from(TAG)
                    .where(TAG.NAME.eq(name))
                    .fetchOne();

            if (record == null) {
                record = DSL.using(conf)
                        .insertInto(TAG, TAG.NAME)
                        .values(name)
                        .returningResult(TAG.ID)
                        .fetchOne();
            }

            DSL.using(conf)
                    .insertInto(TAG_TASK, TAG_TASK.TAG_ID, TAG_TASK.TASK_ID)
                    .values(record.getValue(TAG.ID), taskId)
                    .execute();
        });
    }

    private void addTagsToDataConnection(Long dataConnectionId, Set<String> tagNames) {
        tagNames.forEach((name) -> {
            Record record = DSL.using(conf)
                    .select(TAG.ID).from(TAG)
                    .where(TAG.NAME.eq(name))
                    .fetchOne();

            if (record == null) {
                record = DSL.using(conf)
                        .insertInto(TAG, TAG.NAME)
                        .values(name)
                        .returningResult(TAG.ID)
                        .fetchOne();
            }

            DSL.using(conf)
                    .insertInto(TAG_DATACONNECTION, TAG_DATACONNECTION.TAG_ID,
                            TAG_DATACONNECTION.DATACONNECTION_ID)
                    .values(record.getValue(TAG.ID), dataConnectionId)
                    .execute();
        });

    }

    private void removeTagsFromTask(Long taskId, Set<String> tagsToRemove) {
        DSL.using(conf)
                .delete(TAG_TASK)
                .where(TAG_TASK.TAG_ID.in(
                                select(TAG.ID)
                                .from(TAG)
                                .where(TAG.NAME.in(tagsToRemove))
                ))
                .and(TAG_TASK.TASK_ID.eq(taskId))
                .execute();

    }

    private void removeTagsFromDataConnection(Long dataConnectionId, Set<String> tagsToRemove) {
        DSL.using(conf)
                .delete(TAG_DATACONNECTION)
                .where(TAG_DATACONNECTION.TAG_ID.in(
                        select(TAG.ID)
                                .from(TAG)
                                .where(TAG.NAME.in(tagsToRemove))
                ))
                .and(TAG_DATACONNECTION.DATACONNECTION_ID.eq(dataConnectionId))
                .execute();

    }

    public List<Tag> updateTaskTags(Long taskId, List<String> tagNames) {
        Set<String> oldTags = DSL.using(conf)
                .select()
                .from(TAG)
                .leftJoin(TAG_TASK)
                .on(TAG.ID.eq(TAG_TASK.TAG_ID))
                .where(TAG_TASK.TASK_ID.eq(taskId))
                .fetch()
                .getValues(TAG.NAME).stream().collect(Collectors.toSet());

        Set<String> newTags = new HashSet<>(tagNames);

        Set<String> tagsToRemove = oldTags
                .stream()
                .filter(Predicate.not(newTags::contains))
                .collect(Collectors.toSet());

        if (tagsToRemove.size() > 0) {
            removeTagsFromTask(taskId, tagsToRemove);
        }

        Set<String> tagsToAdd = newTags
                .stream()
                .filter(Predicate.not(oldTags::contains))
                .collect(Collectors.toSet());

        if (tagsToAdd.size() > 0) {
            addTagsToTask(taskId, tagsToAdd);
        }

        return getTaskTags(taskId);
    }


    public List<Tag> updateDataConnectionTags(Long dataConnectionId, List<String> tagNames) {
        Set<String> oldTags = DSL.using(conf)
                .select()
                .from(TAG)
                .leftJoin(TAG_DATACONNECTION)
                .on(TAG.ID.eq(TAG_DATACONNECTION.TAG_ID))
                .where(TAG_DATACONNECTION.DATACONNECTION_ID.eq(dataConnectionId))
                .fetch()
                .getValues(TAG.NAME).stream().collect(Collectors.toSet());

        Set<String> newTags = new HashSet<>(tagNames);

        Set<String> tagsToRemove = oldTags
                .stream()
                .filter(Predicate.not(newTags::contains))
                .collect(Collectors.toSet());

        if (tagsToRemove.size() > 0) {
            removeTagsFromDataConnection(dataConnectionId, tagsToRemove);
        }

        Set<String> tagsToAdd = newTags
                .stream()
                .filter(Predicate.not(oldTags::contains))
                .collect(Collectors.toSet());

        if (tagsToAdd.size() > 0) {
            addTagsToDataConnection(dataConnectionId, tagsToAdd);
        }

        return getDataConnectionTags(dataConnectionId);
    }

}

