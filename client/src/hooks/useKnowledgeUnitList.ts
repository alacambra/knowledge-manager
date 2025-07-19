import { useState, useEffect } from 'preact/hooks';
import { Modal, message } from 'antd';
import { useLocation } from 'preact-iso';
import { KnowledgeUnitRepository2 as KnowledgeUnitRepository } from '../repositories/knowledge.unit.repository2';
import { routes } from '..';
import { KnowledgeUnit } from '../api';

export function useKnowledgeUnitList() {
 const location = useLocation();
 const [knowledgeUnits, setKnowledgeUnits] = useState<KnowledgeUnit[]>([]);
 const [loading, setLoading] = useState(true);

 const loadKnowledgeUnits = async () => {
  try {
   setLoading(true);
   const units = await KnowledgeUnitRepository.listKnowledgeUnits();
   setKnowledgeUnits(units);
  } catch (error) {
   message.error(error.message);
  } finally {
   setLoading(false);
  }
 };

 useEffect(() => {
  loadKnowledgeUnits();
 }, []);

 const handleDeleteKU = (id: string) => {
  Modal.confirm({
   title: 'Are you sure you want to delete this knowledge unit?',
   content: 'This action cannot be undone.',
   async onOk() {
    try {
     await KnowledgeUnitRepository.deleteKnowledgeUnit(id);
     setKnowledgeUnits(prev => prev.filter(ku => ku.id !== id));
     message.success('Knowledge unit deleted successfully');
    } catch (error) {
     message.error('Failed to delete knowledge unit');
    }
   },
  });
 };

 const handleDownloadKU = async (knowledgeUnit: KnowledgeUnit) => {
  try {
   const { blob, filename } = await KnowledgeUnitRepository.downloadKnowledgeUnit(knowledgeUnit.id);
   
   const url = window.URL.createObjectURL(blob);
   const link = document.createElement('a');
   link.href = url;
   link.download = filename;
   link.setAttribute('target', '_blank');
   link.setAttribute('rel', 'noopener noreferrer');
   link.style.display = 'none';
   document.body.appendChild(link);
   link.click();
   document.body.removeChild(link);
   window.URL.revokeObjectURL(url);
   
   message.success(`Downloaded ${knowledgeUnit.name}`);
  } catch (error) {
   message.error(`Failed to download ${knowledgeUnit.name}`);
  }
 };

 const handleEditKU = (knowledgeUnit: KnowledgeUnit) => {
  location.route(routes.editKuPath(knowledgeUnit.id));
 };

 const handleCreateNewKU = () => {
  location.route(routes.createKuPath());
 };

 return {
  knowledgeUnits,
  loading,
  handleDelete: handleDeleteKU,
  handleDownload: handleDownloadKU,
  handleEdit: handleEditKU,
  handleCreateNew: handleCreateNewKU,
  loadKnowledgeUnits
 };
}