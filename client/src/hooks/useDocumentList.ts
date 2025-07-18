import { useState, useEffect } from 'preact/hooks';
import { Modal, message } from 'antd';
import { useLocation } from 'preact-iso';
import { DocumentRepository } from '../repositories/document.repository';
import { routes } from '..';
import { Document } from '../api';

export function useDocumentList() {
  const location = useLocation();
  const [documents, setDocuments] = useState<Document[]>([]);
  const [loading, setLoading] = useState(true);

  const loadDocuments = async () => {
    try {
      setLoading(true);
      const docs = await DocumentRepository.getAllDocuments();
      setDocuments(docs);
    } catch (error) {
      message.error(error.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDocuments();
  }, []);

  const handleDelete = (id: string) => {
    Modal.confirm({
      title: 'Are you sure you want to delete this document?',
      content: 'This action cannot be undone.',
      async onOk() {
        try {
          await DocumentRepository.deleteDocument(id);
          setDocuments(prev => prev.filter(doc => doc.id !== id));
          message.success('Document deleted successfully');
        } catch (error) {
          message.error('Failed to delete document');
        }
      },
    });
  };

  const handleDownload = (document: Document) => {
    message.info(`Downloading ${document.title}...`);
    // TODO: Implement download functionality
  };

  const handleEdit = (document: Document) => {
    message.info(`Editing ${document.title}...`);
    // TODO: Implement edit functionality
  };

  const handleCreateNew = () => {
    location.route(routes.createDocumentPath());
  };

  return {
    documents,
    loading,
    handleDelete,
    handleDownload,
    handleEdit,
    handleCreateNew,
    loadDocuments
  };
}