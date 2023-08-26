package fi.jubic.quanta.controller;

import fi.jubic.quanta.dao.TagDao;
import fi.jubic.quanta.models.Tag;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class TagController {
    private final TagDao tagDao;

    @Inject
    TagController(
            TagDao tagDao
    ) {
        this.tagDao = tagDao;
    }

    public List<Tag> getAll() {
        return tagDao.getAll();
    }
}
