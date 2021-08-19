package fi.jubic.quanta.controller;

import fi.jubic.quanta.config.Configuration;
import fi.jubic.quanta.dao.TagDao;
import fi.jubic.quanta.domain.TagDomain;
import fi.jubic.quanta.models.Tag;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;

@Singleton
public class TagController {
    private final TagDao tagDao;
    private final TagDomain tagDomain;

    @Inject
    TagController(
            TagDao tagDao,
            TagDomain tagDomain,
            Configuration configuration
    ) {
        this.tagDao = tagDao;
        this.tagDomain = tagDomain;
    }

    public List<Tag> getAll() {
        return tagDao.getAll();
    }

    public List<Tag> updateTaskTags(Long taskId, Set<String> tagNames) {
        return tagDao.updateTaskTags(
                taskId, tagDomain.validate(tagNames)
        );
    }

    public List<Tag> updateDataConnectionTags(Long taskId, Set<String> tagNames) {
        return tagDao.updateDataConnectionTags(
                taskId, tagDomain.validate(tagNames)
        );
    }
}
