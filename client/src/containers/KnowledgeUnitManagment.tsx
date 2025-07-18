import { KnowledgeUnitForm } from '../components/KnowledgeUnitForm';

interface CreateKUContainerProps {
  onSuccess?: () => void;
}

export function CreateKUContainer({ onSuccess }: CreateKUContainerProps) {
  return (
    <KnowledgeUnitForm
      onSuccess={onSuccess}
    />
  );
}