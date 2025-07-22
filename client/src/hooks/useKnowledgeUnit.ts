import { useState, useEffect } from 'preact/hooks';
import { message } from 'antd';
import { useLocation } from 'preact-iso';
import { KnowledgeUnitRepository2 } from '../repositories/knowledge.unit.repository2';
import { DocumentRepository } from '../repositories/document.repository';
import type { KnowledgeUnitRequest, DocumentRequest, Document, KnowledgeUnitWithResourcesResponse, DocumentGroup } from '../api/models';
import { routes } from '..';

interface UseKnowledgeUnitEditConfig {
  onSuccess?: () => void;
  id?: string;
}

function transformResourcesResponseToDocuments(response: KnowledgeUnitWithResourcesResponse): Document[] {
  if (!response.resources) {
    return [];
  }
  
  const documents: Document[] = [];
  
  response.resources.forEach(resource => {
    if (resource.documentGroups) {
      resource.documentGroups.forEach(documentGroup => {
        // Transform DocumentGroup to Document for UI compatibility
        // Note: DocumentGroup has different structure than Document
        // This mapping may need refinement based on actual domain requirements
        const document: Document = {
          id: documentGroup.id,
          title: documentGroup.uri || 'Untitled Document', // Using URI as title - may need better mapping
          content: `Document Group: ${documentGroup.uri || 'Unknown'}`, // Placeholder content
          fileName: documentGroup.uri || 'unknown-file', // Using URI as filename - required property
          uri: documentGroup.uri,
          createdAt: documentGroup.createdAt ? (documentGroup.createdAt instanceof Date ? documentGroup.createdAt : new Date(documentGroup.createdAt)) : undefined,
          updatedAt: documentGroup.updatedAt ? (documentGroup.updatedAt instanceof Date ? documentGroup.updatedAt : new Date(documentGroup.updatedAt)) : undefined,
          embedding: null,
          metadata: undefined
        };
        documents.push(document);
      });
    }
  });
  
  return documents;
}

export function useKnowledgeUnitEdit(config: UseKnowledgeUnitEditConfig = {}) {
  const location = useLocation();
  const { id } = config;
  const isEditMode = Boolean(id);

  // Common state
  const [loading, setLoading] = useState(isEditMode);
  const [saving, setSaving] = useState(false);

  // Creation mode state
  const [documents, setDocuments] = useState<DocumentRequest[]>([]);

  // Edit mode state
  const [kuData, setKuData] = useState<KnowledgeUnitWithResourcesResponse | null>(null);
  const [newDocuments, setNewDocuments] = useState<DocumentRequest[]>([]);
  const [allDocuments, setAllDocuments] = useState<Document[]>([]);
  const [addedDocumentIds, setAddedDocumentIds] = useState<string[]>([]);
  const [removedDocumentIds, setRemovedDocumentIds] = useState<string[]>([]);

  // Load initial data for edit mode
  useEffect(() => {
    if (isEditMode && id) {
      loadKnowledgeUnit();
    } else {
      setLoading(false);
    }
  }, [id, isEditMode]);

  const loadKnowledgeUnit = async () => {
    if (!id) return;
    
    try {
      setLoading(true);
      const data = await KnowledgeUnitRepository2.getKnowledgeUnitWithResources(id);
      setKuData(data);
      
      // Transform the hierarchical resources data into a flat documents array for UI compatibility
      const documents = transformResourcesResponseToDocuments(data);
      setAllDocuments(documents);
    } catch (error) {
      message.error('Failed to load knowledge unit');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const loadAllDocuments = async () => {
    try {
      const documents = await DocumentRepository.getAllDocuments();
      setAllDocuments(documents);
    } catch (error) {
      console.error('Failed to load documents:', error);
    }
  };

  const handleSubmit = async (values: { name?: string; description?: string }) => {
    try {
      setSaving(true);

      if (isEditMode && id) {
        // Edit mode
        const request: KnowledgeUnitRequest = {
          name: values.name,
          description: values.description,
          newDocuments: newDocuments.length > 0 ? newDocuments : undefined,
          addedDocumentsIds: addedDocumentIds.length > 0 ? addedDocumentIds : undefined,
          removedDocumentsIds: removedDocumentIds.length > 0 ? removedDocumentIds : undefined
        };

        await KnowledgeUnitRepository2.updateKnowledgeUnit(id, request);
        message.success('Knowledge unit updated successfully');

        // Reset the change tracking
        setNewDocuments([]);
        setAddedDocumentIds([]);
        setRemovedDocumentIds([]);

        // Reload the data
        await loadKnowledgeUnit();
      } else {
        // Creation mode
        const requestData: KnowledgeUnitRequest = {
          name: values.name,
          description: values.description,
          newDocuments: documents.length > 0 ? documents : undefined
        };
        
        await KnowledgeUnitRepository2.createKnowledgeUnit(requestData);
        message.success('Knowledge unit created successfully!');
        setDocuments([]);
      }

      if (config.onSuccess) {
        config.onSuccess();
      } else {
        location.route('/');
      }
    } catch (error) {
      const errorMessage = isEditMode ? 'Failed to update knowledge unit' : 'Failed to create knowledge unit';
      message.error(errorMessage);
      console.error(error);
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    location.route('/');
  };

  const handleAddDocument = () => {
    location.route(routes.createDocumentPath());
  };

  const handleRemoveDocumentByIndex = (index: number) => {
    setDocuments(docs => docs.filter((_, i) => i !== index));
  };

  const addDocument = (document: DocumentRequest) => {
    setDocuments(prev => [...prev, document]);
  };

  const handleRemoveDocument = (docId: string) => {
    // If it's a new document, just remove it from the new documents array
    const newDocIndex = newDocuments.findIndex((_, index) => `new-${index}` === docId);
    if (newDocIndex !== -1) {
      setNewDocuments(prev => prev.filter((_, index) => index !== newDocIndex));
      return;
    }

    // If it's an added document, remove it from added list
    if (addedDocumentIds.includes(docId)) {
      setAddedDocumentIds(prev => prev.filter(id => id !== docId));
      return;
    }

    // If it's an original document, add it to removed list
    setRemovedDocumentIds(prev => [...prev, docId]);
  };

  const handleAddExistingDocument = (documentId: string) => {
    if (!kuData) return;
    
    // Get the transformed documents from the resources
    const originalDocuments = transformResourcesResponseToDocuments(kuData);
    
    if (!addedDocumentIds.includes(documentId) &&
        !originalDocuments.some((doc: Document) => doc.id === documentId) &&
        !removedDocumentIds.includes(documentId)) {
      setAddedDocumentIds(prev => [...prev, documentId]);
      // Remove from removed list if it was there
      setRemovedDocumentIds(prev => prev.filter(id => id !== documentId));
    }
  };

  const handleAddNewDocument = (values: { name: string; content: string }) => {
    setNewDocuments(prev => [...prev, { name: values.name, content: values.content }]);
  };

  // Get current documents to display (edit mode)
  const getCurrentDocuments = () => {
    if (!kuData) return [];

    // Get the transformed documents from the resources
    const originalDocuments = transformResourcesResponseToDocuments(kuData);
    
    // Start with original documents
    let currentDocs = originalDocuments.filter((doc: Document) => !removedDocumentIds.includes(doc.id!)) ?? [];

    // Add newly added existing documents
    const addedDocs = allDocuments.filter(doc => addedDocumentIds.includes(doc.id));
    currentDocs = [...currentDocs, ...addedDocs];

    // Add new documents (with temporary IDs)
    const newDocs: Document[] = newDocuments.map((doc, index) => ({
      id: `new-${index}`,
      title: doc.name || 'Untitled Document',
      content: doc.content || '',
      fileName: doc.name || `new-document-${index}`,
      uri: undefined,
      createdAt: undefined,
      updatedAt: undefined,
      embedding: null,
      metadata: undefined
    }));
    currentDocs = [...currentDocs, ...newDocs];

    return currentDocs;
  };

  // Get available documents for adding (edit mode)
  const getAvailableDocuments = () => {
    if (!kuData) return allDocuments;

    // Get the transformed documents from the resources
    const originalDocuments = transformResourcesResponseToDocuments(kuData);
    const currentDocIds = originalDocuments.map((doc: Document) => doc.id);
    
    return allDocuments.filter(doc =>
      !currentDocIds.includes(doc.id) &&
      !addedDocumentIds.includes(doc.id) &&
      removedDocumentIds.includes(doc.id!) // Allow re-adding removed documents
    );
  };

  // Listen for document data from URL state (creation mode)
  useEffect(() => {
    if (!isEditMode && location.query.newDocument) {
      try {
        const newDoc = JSON.parse(decodeURIComponent(location.query.newDocument));
        addDocument(newDoc);
        // Clear the query parameter
        location.route(location.path);
      } catch (error) {
        console.error('Error parsing document data:', error);
      }
    }
  }, [location.query, isEditMode]);

  return {
    // Common
    isEditMode,
    loading,
    saving,
    handleSubmit,
    handleCancel,
    
    // Creation mode
    documents,
    handleAddDocument,
    handleRemoveDocumentByIndex,
    addDocument,
    
    // Edit mode
    kuData,
    newDocuments,
    allDocuments,
    addedDocumentIds,
    removedDocumentIds,
    loadAllDocuments,
    handleRemoveDocument,
    handleAddExistingDocument,
    handleAddNewDocument,
    getCurrentDocuments,
    getAvailableDocuments
  };
}

// Keep the old hook for backward compatibility
export function useKnowledgeUnit(config: UseKnowledgeUnitEditConfig = {}) {
  return useKnowledgeUnitEdit(config);
}