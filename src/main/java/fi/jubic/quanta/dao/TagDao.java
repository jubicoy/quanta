package fi.jubic.quanta.dao;

import fi.jubic.quanta.models.TagAssignment;
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

    /* public List<DataConnection> enrichDataConnectionTags(List<DataConnection> dataConnections) {
        // Fetch all assignments for all data connections
        List<Long> dataConnIds = dataConnections.stream()
                .map(DataConnection::getId).collect(Collectors.toList());
        Map<Long, Set<String>> tagsMap = getAllDataConnectionTags(dataConnIds).stream()
                .collect(Collectors.
                groupingBy(TagAssignment::getParentId,

                        Collectors.
                        mapping(ta -> ta.getTag().getName(), Collectors.toSet())));

        return dataConnections.stream().map(conn -> conn.toBuilder().
        setTags(tagsMap.getOrDefault(conn.getId(),

         Collections.emptyList())).build());


        // map through data connections and assign tags into the objects
    }

    public List<Task> enrichTaskTags(List<Task> tasks) {

    }

    // TODO: Remove
    public List<TagAssignment> getDataConnectionTags(Long dataConnectionId) {
        return DSL.using(conf)
                .select()
                .from(TAG)
                .leftJoin(TAG_DATACONNECTION)
                .on(TAG.ID.eq(TAG_DATACONNECTION.TAG_ID))
                .where(TAG_DATACONNECTION.DATACONNECTION_ID.eq(dataConnectionId))
                .fetchStream()
                .collect(TagAssignment.taskTagMapper);
    }

    // TODO: Remove
    public List<TagAssignment> getTaskTags(Long taskId) {
        return DSL.using(conf)
                .select()
                .from(TAG)
                .leftJoin(TAG_TASK)
                .on(TAG.ID.eq(TAG_TASK.TAG_ID))
                .where(TAG_TASK.TASK_ID.eq(taskId))
                .fetchStream()
                .collect(TagAssignment.dataConnectionTagMapper);
    }

    /* public Task setTaskTags(Task task) {
        List<String> existingTags = getTaskTags(task.getId());
        List<String> newTags = task.getTags();

    }


    public DataConnection setDataConnectionTags(DataConnection dataConnection) {

    }
    */

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

    public void updateTaskTags(Long taskId, List<String> tagNames) {
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

        // return getTaskTags(taskId);
    }


    public void updateDataConnectionTags(Long dataConnectionId, List<String> tagNames) {
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

        // return getDataConnectionTags(dataConnectionId);
    }

}

