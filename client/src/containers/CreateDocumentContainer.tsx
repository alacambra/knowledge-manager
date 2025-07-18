import { useState } from 'preact/hooks';
import { message } from 'antd';
import { useLocation } from 'preact-iso';
import { DocumentForm } from '../components/DocumentForm';
import type { DocumentRequest } from '../api/models';

interface DocumentFormData {
  name: string;
  content: string;
}

interface CreateDocumentContainerProps {
  onSuccess?: () => void;
}

export function CreateDocumentContainer({ onSuccess }: CreateDocumentContainerProps) {
  const location = useLocation();
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (values: DocumentFormData) => {
    try {
      setLoading(true);
      
      // Convert form data to DocumentRequest format
      const documentData: DocumentRequest = {
        name: values.name,
        content: values.content
      };
      
      // Return to Knowledge Unit form with document data
      const encodedData = encodeURIComponent(JSON.stringify(documentData));
      location.route(`/create-ku?newDocument=${encodedData}`);
      
      message.success('Document added successfully!');
      
      if (onSuccess) {
        onSuccess();
      }
    } catch (error) {
      console.error('Error adding document:', error);
      message.error('Failed to add document');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    location.route('/create-ku');
  };

  return (
    <DocumentForm
      onSubmit={handleSubmit}
      onCancel={handleCancel}
      loading={loading}
    />
  );
}