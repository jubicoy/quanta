// package fi.jubic.quanta.controller;

// import fi.jubic.quanta.config.Configuration;
// import fi.jubic.quanta.dao.TagDao;
// import fi.jubic.quanta.domain.TagDomain;
// import fi.jubic.quanta.models.TagAssignment;

// import javax.inject.Inject;
// import javax.inject.Singleton;
// import java.util.List;

// @Singleton
// public class TagController {
//     private final TagDao tagDao;
//     private final TagDomain tagDomain;
//     private final Configuration configuration;
//     private final org.jooq.Configuration conf;

//     @Inject
//     TagController(
//             TagDao tagDao,
//             TagDomain tagDomain,
//             Configuration configuration
//     ) {
//         this.tagDao = tagDao;
//         this.tagDomain = tagDomain;
//         this.conf = configuration.getJooqConfiguration().getConfiguration();
//         this.configuration = configuration;
//     }

//     /* public List<TagAssignment> getAllTaskTags() {
//         return tagDao.getAllTaskTags();
//     }
//  */
//     /*public List<TagAssignment> getALlDataConnectionTags() {
//         return tagDao.getAllDataConnectionTags();
//     }

//     public List<TagAssignment> getDataConnectionTags(Long id) {
//         return tagDao.getDataConnectionTags(id);
//     }

//     public List<TagAssignment> getTaskTags(Long id) {
//         return tagDao.getTaskTags(id);
//     }

//     public List<Long> searchTasks(List<Long> tagIds) {
//         return tagDao.searchTasks(tagIds);
//     }

//     public List<Long> searchDataConnections(List<Long> tagIds) {
//         return tagDao.searchDataConnections(tagIds);
//     }

//     public List<TagAssignment> updateTaskTags(Long taskId, List<String> tagNames) {
//         return tagDao.updateTaskTags(
//                  taskId, tagDomain.validate(tagNames)
//         );
//     }


//     public List<TagAssignment> updateDataConnectionTags(Long taskId, List<String> tagNames) {
//         return tagDao.updateDataConnectionTags(
//                 taskId, tagDomain.validate(tagNames)
//         );
//     }
//     */
// }
