import { Button, List, Space, Typography } from 'antd';
import { EditOutlined, DeleteOutlined, DownloadOutlined, PlusOutlined } from '@ant-design/icons';
import { useDocumentList } from '../hooks/useDocumentList';

const { Title } = Typography;

export function DocumentList() {
  const {
    documents,
    loading,
    handleDelete,
    handleDownload,
    handleEdit,
    handleCreateNew
  } = useDocumentList();

 return (
  <div style={{ padding: '24px', height: '100vh', overflow: 'auto' }}>
   <div style={{ marginBottom: '24px' }}>
    <Title level={2} style={{ marginBottom: '16px' }}>Documents</Title>
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
     Create new Document
    </Button>
   </div>

   <List
    loading={loading}
    dataSource={documents}
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
        {item.title}
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