import { DocumentForm } from '../components/DocumentForm';

interface CreateDocumentContainerProps {
  onSuccess?: () => void;
}

export function CreateDocumentContainer({ onSuccess }: CreateDocumentContainerProps) {
  return (
    <DocumentForm
      mode="standalone"
      onSuccess={onSuccess}
    />
  );
}