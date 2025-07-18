import { useState, useEffect } from 'preact/hooks';
import { Button, List, Space, Typography, Modal, Input, message } from 'antd';
import { EditOutlined, DeleteOutlined, DownloadOutlined, PlusOutlined } from '@ant-design/icons';
import { useLocation } from 'preact-iso';
import { KnowledgeUnitRepository2 as KnowledgeUnitRepository } from '../repositories/knowledge.unit.repository2';
import { routes } from '..';
import { KnowledgeUnit } from '../api';

const { Title } = Typography;


export function KnowledgeUnitList() {
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

 const handleDelete = (id: string) => {
  Modal.confirm({
   title: 'Are you sure you want to delete this knowledge unit?',
   content: 'This action cannot be undone.',
   async onOk() {
    try {
     const success = await KnowledgeUnitRepository.deleteKnowledgeUnit(id);
     if (success) {
      setKnowledgeUnits(prev => prev.filter(ku => ku.id !== id));
      message.success('Knowledge unit deleted successfully');
     } else {
      message.error('Failed to delete knowledge unit');
     }
    } catch (error) {
     message.error('Failed to delete knowledge unit');
    }
   },
  });
 };

 const handleDownload = (knowledgeUnit: KnowledgeUnit) => {
  // TODO: Implement download functionality
  message.info(`Downloading ${knowledgeUnit.name}...`);
 };

 const handleEdit = (knowledgeUnit: KnowledgeUnit) => {
  location.route(`/edit/${knowledgeUnit.id}`);
 };

 const handleCreateNew = () => {
  location.route(routes.createKuPath());
 };

 return (
  <div style={{ padding: '24px', height: '100vh', overflow: 'auto' }}>
   <div style={{ marginBottom: '24px' }}>
    <Title level={2} style={{ marginBottom: '16px' }}>Knowledge Units</Title>
    <Button
     type="primary"
     icon={<PlusOutlined />}
     onClick={handleCreateNew}
     style={{
      backgroundColor: '#52c41a',
      borderColor: '#52c41a',
      borderRadius: '8px',
      height: '40px',
      fontSize: '16px'
     }}
    >
     Create new KU
    </Button>
   </div>

   <List
    loading={loading}
    dataSource={knowledgeUnits}
    renderItem={(item) => (
     <List.Item style={{ marginBottom: '8px' }}>
      <div style={{
       display: 'flex',
       width: '100%',
       alignItems: 'center',
       padding: '12px',
       border: '1px solid #d9d9d9',
       borderRadius: '8px',
       backgroundColor: '#f8f9fa'
      }}>
       <div
        style={{
         flex: 1,
         backgroundColor: '#1890ff',
         color: 'white',
         padding: '12px 16px',
         borderRadius: '8px',
         fontSize: '16px',
         fontWeight: '500',
         cursor: 'pointer',
         marginRight: '12px'
        }}
        onClick={() => handleEdit(item)}
       >
        {item.name}
       </div>
       <Space>
        <Button
         type="default"
         icon={<EditOutlined />}
         onClick={() => handleEdit(item)}
         style={{
          backgroundColor: '#52c41a',
          borderColor: '#52c41a',
          color: 'white'
         }}
        >
         EDIT
        </Button>
        <Button
         type="default"
         icon={<DeleteOutlined />}
         onClick={() => handleDelete(item.id)}
         style={{
          backgroundColor: '#ff4d4f',
          borderColor: '#ff4d4f',
          color: 'white'
         }}
        >
         DEL
        </Button>
        <Button
         type="default"
         icon={<DownloadOutlined />}
         onClick={() => handleDownload(item)}
         style={{
          backgroundColor: '#52c41a',
          borderColor: '#52c41a',
          color: 'white'
         }}
        >
         DL
        </Button>
       </Space>
      </div>
     </List.Item>
    )}
   />
  </div>
 );
}