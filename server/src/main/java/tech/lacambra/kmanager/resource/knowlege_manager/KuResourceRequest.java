package tech.lacambra.kmanager.resource.knowlege_manager;

import java.util.List;
import java.util.UUID;

public record KuResourceRequest(
  String name,
  String description,
  List<DocumentGroupRequest> newDocumentGroups,
  List<UUID> addedDocumentsGroupsIds,
  List<UUID> removedDocumentsGroupsIds
  
  ) {
}
