package tech.lacambra.kmanager.resource.knowlege_manager;

import java.util.List;

public record KnowledgeUnitRequest(
  String name,
  String description,
  List<KuResourceRequest> kuResourceRequests) {
}
