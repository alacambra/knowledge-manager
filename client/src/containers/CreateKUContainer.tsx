import { useState, useEffect } from 'preact/hooks';
import { message } from 'antd';
import { useLocation } from 'preact-iso';
import { KnowledgeUnitRepository2 } from '../repositories/knowledge.unit.repository2';
import { KnowledgeUnitForm } from '../components/KnowledgeUnitForm';
import type { KnowledgeUnitRequest, DocumentRequest } from '../api/models';
import { routes } from '..';

interface CreateKUContainerProps {
  onSuccess?: () => void;
}

export function CreateKUContainer({ onSuccess }: CreateKUContainerProps) {
  const location = useLocation();
  const [loading, setLoading] = useState(false);
  const [documents, setDocuments] = useState<DocumentRequest[]>([]);

  const handleSubmit = async (values: KnowledgeUnitRequest) => {
    try {
      setLoading(true);
      const requestData: KnowledgeUnitRequest = {
        ...values,
        documents: documents
      };
      await KnowledgeUnitRepository2.createKnowledgeUnit(requestData);
      message.success('Knowledge unit created successfully!');
      setDocuments([]);
      
      if (onSuccess) {
        onSuccess();
      } else {
        location.route('/');
      }
    } catch (error) {
      console.error('Error creating knowledge unit:', error);
      message.error('Failed to create knowledge unit');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    location.route('/');
  };

  const handleAddDocument = () => {
    location.route(routes.createDocumentPath());
  };

  const handleRemoveDocument = (index: number) => {
    setDocuments(docs => docs.filter((_, i) => i !== index));
  };

  // Listen for document data from URL state
  useEffect(() => {
    if (location.query.newDocument) {
      try {
        const newDoc = JSON.parse(decodeURIComponent(location.query.newDocument));
        setDocuments(prev => [...prev, newDoc]);
        // Clear the query parameter
        location.route(location.path);
      } catch (error) {
        console.error('Error parsing document data:', error);
      }
    }
  }, [location.query]);

  return (
    <KnowledgeUnitForm
      onSubmit={handleSubmit}
      onCancel={handleCancel}
      onAddDocument={handleAddDocument}
      onRemoveDocument={handleRemoveDocument}
      loading={loading}
      documents={documents}
    />
  );
}