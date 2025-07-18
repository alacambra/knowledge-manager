import { useState } from 'preact/hooks';
import { message } from 'antd';
import { useLocation } from 'preact-iso';
import type { DocumentRequest } from '../api/models';
import { DocumentRepository } from '../repositories/document.repository';
import { routes } from '..';

interface DocumentFormData {
  name: string;
  content: string;
}

interface UseDocumentConfig {
  mode: 'embedded' | 'standalone';
  onSuccess?: (document: DocumentRequest) => void;
  onCancel?: () => void;
}

export function useDocument(config: UseDocumentConfig) {
  const location = useLocation();
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (values: DocumentFormData) => {
    try {
      setLoading(true);
      
      const documentData: DocumentRequest = {
        name: values.name,
        content: values.content
      };
      
      if (config.mode === 'embedded') {
        // For embedded mode, call the success callback with the document data
        if (config.onSuccess) {
          config.onSuccess(documentData);
        }
      } else {
        // For standalone mode, upload the document and navigate back to documents list
        await DocumentRepository.uploadDocuments([documentData]);
        location.route(routes.documentsPath());
      }
      
      message.success('Document added successfully!');
      
    } catch (error) {
      console.error('Error adding document:', error);
      message.error('Failed to add document');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    if (config.onCancel) {
      config.onCancel();
    } else {
      // Default behavior based on mode
      if (config.mode === 'standalone') {
        location.route(routes.documentsPath());
      }
    }
  };

  return {
    loading,
    handleSubmit,
    handleCancel
  };
}