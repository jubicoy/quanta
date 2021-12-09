package fi.jubic.quanta.dao;

import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.Tag;
import fi.jubic.quanta.models.TagAssignment;
import fi.jubic.quanta.models.Task;
import org.jooq.Record;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public List<TagAssignment> getAllTaskTags(List<Long> taskIds) {
        return DSL.using(conf)
                .select()
                .from(TAG_TASK)
                .where(TAG_TASK.TASK_ID.in(taskIds))
                .fetchStream()
                .collect(TagAssignment.tagTaskMapper);
    }

    public List<TagAssignment> getAllDataConnectionTags(List<Long> dataConnIds) {
        return DSL.using(conf)
                .select()
                .from(TAG_DATACONNECTION)
                .where(TAG_DATACONNECTION.DATACONNECTION_ID.in(dataConnIds))
                .fetchStream()
                .collect(TagAssignment.tagDataConnectionMapper);
    }

    public List<DataConnection> enrichDataConnectionTags(List<DataConnection> dataConnections) {
        // Fetch all assignments for all data connections
        List<Long> dataConnIds = dataConnections
                .stream()
                .map(DataConnection::getId)
                .collect(Collectors.toList());

        // map through data connections and assign tags into the objects
        Map<Long, Set<String>> tagsMap = getAllDataConnectionTags(dataConnIds)
                .stream()
                .collect(Collectors.groupingBy(TagAssignment::getParentId,
                        Collectors.mapping(ta -> ta.getTag().getName(), Collectors.toSet())));

        return dataConnections.stream()
                .map(dataConnection -> dataConnection.toBuilder()
                .setTags(
                        tagsMap.get(dataConnection.getId())
                ).build()).collect(Collectors.toList());

    }

    public List<Task> enrichTaskTags(List<Task> tasks) {
        // Fetch all assignments for all data connections
        List<Long> taskIds = tasks
                .stream()
                .map(Task::getId)
                .collect(Collectors.toList());

        // map through data connections and assign tags into the objects
        Map<Long, Set<String>> tagsMap = getAllTaskTags(taskIds)
                .stream()
                .collect(Collectors.groupingBy(TagAssignment::getParentId,
                        Collectors.mapping(ta -> ta.getTag().getName(), Collectors.toSet())));

        return tasks.stream()
                .map(task -> task.toBuilder()
                        .setTags(
                                tagsMap.get(task.getId())
                        ).build()).collect(Collectors.toList());

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

    public Task updateTaskTags(Task task, List<String> tagNames) {
        Set<String> oldTags = DSL.using(conf)
                .select()
                .from(TAG)
                .leftJoin(TAG_TASK)
                .on(TAG.ID.eq(TAG_TASK.TAG_ID))
                .where(TAG_TASK.TASK_ID.eq(task.getId()))
                .fetch()
                .getValues(TAG.NAME).stream().collect(Collectors.toSet());

        Set<String> newTags = new HashSet<>(tagNames);

        Set<String> tagsToRemove = oldTags
                .stream()
                .filter(Predicate.not(newTags::contains))
                .collect(Collectors.toSet());

        if (tagsToRemove.size() > 0) {
            removeTagsFromTask(task.getId(), tagsToRemove);
        }

        Set<String> tagsToAdd = newTags
                .stream()
                .filter(Predicate.not(oldTags::contains))
                .collect(Collectors.toSet());

        if (tagsToAdd.size() > 0) {
            addTagsToTask(task.getId(), tagsToAdd);
        }

        Set<String> combined = Stream.concat(newTags.stream(), oldTags.stream())
                .collect(Collectors.toSet());

        return task.toBuilder().setTags(combined).build();
    }


    public DataConnection updateDataConnectionTags(DataConnection dataConnection, List<String> tagNames) {
        Set<String> oldTags = DSL.using(conf)
                .select()
                .from(TAG)
                .leftJoin(TAG_DATACONNECTION)
                .on(TAG.ID.eq(TAG_DATACONNECTION.TAG_ID))
                .where(TAG_DATACONNECTION.DATACONNECTION_ID.eq(dataConnection.getId()))
                .fetch()
                .getValues(TAG.NAME).stream().collect(Collectors.toSet());

        Set<String> newTags = new HashSet<>(tagNames);

        Set<String> tagsToRemove = oldTags
                .stream()
                .filter(Predicate.not(newTags::contains))
                .collect(Collectors.toSet());

        if (tagsToRemove.size() > 0) {
            removeTagsFromDataConnection(dataConnection.getId(), tagsToRemove);
        }

        Set<String> tagsToAdd = newTags
                .stream()
                .filter(Predicate.not(oldTags::contains))
                .collect(Collectors.toSet());

        if (tagsToAdd.size() > 0) {
            addTagsToDataConnection(dataConnection.getId(), tagsToAdd);
        }

        Set<String> combined = Stream.concat(newTags.stream(), oldTags.stream())
                .collect(Collectors.toSet());

        return dataConnection.toBuilder().setTags(combined).build();
    }

}

