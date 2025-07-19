import { DocumentForm } from '../components/DocumentForm';

interface DocumentsManagementProps {
  onSuccess?: () => void;
}

export function DocumentsManagementPage({ onSuccess }: DocumentsManagementProps) {
  return (
    <DocumentForm
      mode="standalone"
      onSuccess={onSuccess}
    />
  );
}