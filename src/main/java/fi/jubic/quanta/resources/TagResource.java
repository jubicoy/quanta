// package fi.jubic.quanta.resources;

// import fi.jubic.quanta.controller.TagController;
// import fi.jubic.quanta.models.TagAssignment;

// import javax.annotation.security.RolesAllowed;
// import javax.inject.Inject;
// import javax.inject.Singleton;
// import javax.ws.rs.GET;
// import javax.ws.rs.PUT;
// import javax.ws.rs.Path;
// import javax.ws.rs.PathParam;
// import javax.ws.rs.Produces;
// import javax.ws.rs.QueryParam;
// import javax.ws.rs.core.MediaType;
// import java.util.List;

// @Singleton
// @Path("tags")
// @RolesAllowed({"ADMIN", "USER"})
// @Produces(MediaType.APPLICATION_JSON)
// public class TagResource {
//     private final TagController tagController;

//     @Inject
//     TagResource(
//             TagController tagController
//     ) {
//         this.tagController = tagController;
//     }

//     @GET
//     @Path("tasks/all")
//     public List<TagAssignment> getAllTaskTags() {
//         return tagController.getAllTaskTags();
//     }

//     @GET
//     @Path("data-connections/all")
//     public List<TagAssignment> getAllDataConnectionTags() {
//         return tagController.getALlDataConnectionTags();
//     }

//     @GET
//     @Path("tasks")
//     public List<Long> searchTasks(
//             @QueryParam("ids") List<Long> tagIds
//     ) {
//         return tagController.searchTasks(tagIds);
//     }

//     @GET
//     @Path("data-connections")
//     public List<Long> searchDataConnections(
//             @QueryParam("ids") List<Long> tagIds
//     ) {
//         return tagController.searchDataConnections(tagIds);
//     }

//     @GET
//     @Path("data-connections/{id}")
//     public List<TagAssignment> getDataConnectionTags(
//             @PathParam("id") Long dataConnectionId
//     ) {
//         return tagController.getDataConnectionTags(
//                 dataConnectionId
//         );
//     }

//     @GET
//     @Path("tasks/{id}")
//     public List<TagAssignment> getTaskTags(
//             @PathParam("id") Long taskId
//     ) {
//         return tagController.getTaskTags(
//                 taskId
//         );
//     }

//     @PUT
//     @Path("data-connections/{id}")
//     public List<TagAssignment> updateDataConnectionTags(
//             @PathParam("id") Long dataConnectionId,
//             List<String> tagNames
//     ) {
//         return tagController.updateDataConnectionTags(
//                 dataConnectionId,
//                 tagNames
//         );
//     }

//     @PUT
//     @Path("tasks/{id}")
//     public List<TagAssignment> updateTaskTags(
//             @PathParam("id") Long taskId,
//             List<String> tagNames
//     ) {
//         return tagController.updateTaskTags(
//                 taskId,
//                 tagNames
//         );
//     }
// }
