import { DocumentForm } from '../components/DocumentForm';

interface DocumentsManagementProps {
  onSuccess?: () => void;
}

export function DocumentsManagement({ onSuccess }: DocumentsManagementProps) {
  return (
    <DocumentForm
      mode="standalone"
      onSuccess={onSuccess}
    />
  );
}