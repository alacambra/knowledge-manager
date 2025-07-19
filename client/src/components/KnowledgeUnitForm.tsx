import { useEffect, useState } from 'preact/hooks';
import { Button, Form, Input, Typography, Card, List, Modal, Spin } from 'antd';
import { SaveOutlined, ArrowLeftOutlined, PlusOutlined, DeleteOutlined, FileAddOutlined } from '@ant-design/icons';
import { useKnowledgeUnitEdit } from '../hooks/useKnowledgeUnit';

const { Title } = Typography;
const { TextArea } = Input;

interface KnowledgeUnitEditProps {
 id?: string;
}

export function KnowledgeUnitEdit({ id }: KnowledgeUnitEditProps) {
 const [form] = Form.useForm();
 const [newDocForm] = Form.useForm();

 const {
  isEditMode,
  loading,
  saving,
  handleSubmit,
  handleCancel,
  // Edit mode specific
  kuData,
  newDocuments,
  allDocuments,
  addedDocumentIds,
  removedDocumentIds,
  loadAllDocuments,
  handleRemoveDocument,
  handleAddExistingDocument,
  handleAddNewDocument,
  getCurrentDocuments,
  getAvailableDocuments,
  // Creation mode specific
  documents,
  handleAddDocument,
  handleRemoveDocumentByIndex,
  addDocument
 } = useKnowledgeUnitEdit({ id });

 // State for modals
 const [isAddDocumentModalVisible, setIsAddDocumentModalVisible] = useState(false);
 const [isNewDocumentModalVisible, setIsNewDocumentModalVisible] = useState(false);

 // Set form values when data is loaded (edit mode)
 useEffect(() => {
  if (isEditMode && kuData) {
   form.setFieldsValue({
    name: kuData.knowledgeUnit.name,
    description: kuData.knowledgeUnit.description
   });
  }
 }, [kuData, isEditMode, form]);

 const onAddExistingDocument = (documentId: string) => {
  handleAddExistingDocument(documentId);
  setIsAddDocumentModalVisible(false);
 };

 const onAddNewDocument = (values: { name: string; content: string }) => {
  handleAddNewDocument(values);
  newDocForm.resetFields();
  setIsNewDocumentModalVisible(false);
 };

 if (loading) {
  return (
   <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}>
    <Spin size="large" />
   </div>
  );
 }

 if (isEditMode && !kuData) {
  return (
   <div style={{ padding: '24px', textAlign: 'center' }}>
    <Title level={3}>Knowledge Unit not found</Title>
    <Button onClick={handleCancel}>Back to Knowledge Units</Button>
   </div>
  );
 }

 // Get documents to display based on mode
 const getDocumentsToDisplay = () => {
  if (isEditMode) {
   return getCurrentDocuments();
  }
  return documents.map((doc, index) => ({
   id: index.toString(),
   title: doc.name,
   content: doc.content
  }));
 };

 // Get document removal handler based on mode
 const getRemoveHandler = (docId: string, index?: number) => {
  if (isEditMode) {
   return () => handleRemoveDocument(docId);
  }
  return () => handleRemoveDocumentByIndex(index!);
 };

 return (
  <div style={{ padding: '24px', maxWidth: '800px', margin: '0 auto' }}>
   <div style={{ marginBottom: '24px' }}>
    <Button
     type="text"
     icon={<ArrowLeftOutlined />}
     onClick={handleCancel}
     style={{ marginBottom: '16px' }}
    >
     Back to Knowledge Units
    </Button>
    <Title level={2}>{isEditMode ? 'Edit Knowledge Unit' : 'Create Knowledge Unit'}</Title>
   </div>

   <Card>
    <Form
     form={form}
     layout="vertical"
     onFinish={handleSubmit}
     autoComplete="off"
    >
     <Form.Item
      label="Name"
      name="name"
      rules={[
       { required: true, message: 'Please enter a name for the knowledge unit' },
       { max: 100, message: 'Name must not exceed 100 characters' }
      ]}
     >
      <Input
       placeholder="Enter knowledge unit name"
       size="large"
       style={{ borderRadius: '8px' }}
      />
     </Form.Item>

     <Form.Item
      label="Description"
      name="description"
      rules={[
       { required: true, message: 'Please enter a description' },
       { max: 1000, message: 'Description must not exceed 1000 characters' }
      ]}
     >
      <TextArea
       placeholder="Enter a detailed description of the knowledge unit"
       rows={6}
       size="large"
       style={{ borderRadius: '8px' }}
      />
     </Form.Item>

     <Form.Item label="Documents">
      <div style={{ marginBottom: '16px', display: 'flex', gap: '12px' }}>
       <Button
        type="default"
        onClick={() => setIsNewDocumentModalVisible(true)}
        icon={<PlusOutlined />}
        style={{ borderRadius: '8px' }}
       >
        Create New Document
       </Button>
       {isEditMode && (
        <Button
         type="default"
         onClick={() => setIsAddDocumentModalVisible(true)}
         icon={<FileAddOutlined />}
         style={{ borderRadius: '8px' }}
        >
         Add Existing Document
        </Button>
       )}
       {!isEditMode && (
        <Button
         type="default"
         onClick={handleAddDocument}
         icon={<FileAddOutlined />}
         style={{ borderRadius: '8px' }}
        >
         Add Existing Document
        </Button>
       )}
      </div>

      <List
       size="small"
       dataSource={getDocumentsToDisplay()}
       renderItem={(doc: any, index: number) => (
        <List.Item
         actions={[
          <Button
           type="text"
           danger
           icon={<DeleteOutlined />}
           onClick={getRemoveHandler(doc.id, index)}
          />
         ]}
         style={{
          backgroundColor: doc.id.startsWith('new-') ? '#f6ffed' :
           (isEditMode && addedDocumentIds.includes(doc.id)) ? '#e6f7ff' : '#fff'
         }}
        >
         <List.Item.Meta
          title={
           <span>
            {doc.title}
            {doc.id.startsWith('new-') && <span style={{ color: '#52c41a', marginLeft: '8px' }}>(New)</span>}
            {isEditMode && addedDocumentIds.includes(doc.id) && <span style={{ color: '#1890ff', marginLeft: '8px' }}>(Added)</span>}
           </span>
          }
          description={doc.content?.substring(0, 100) + (doc.content && doc.content.length > 100 ? '...' : '')}
         />
        </List.Item>
       )}
      />
     </Form.Item>

     <Form.Item style={{ marginBottom: 0 }}>
      <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
       <Button
        type="default"
        onClick={handleCancel}
        size="large"
        style={{ borderRadius: '8px' }}
       >
        Cancel
       </Button>
       <Button
        type="primary"
        htmlType="submit"
        loading={saving}
        icon={<SaveOutlined />}
        size="large"
        style={{
         backgroundColor: '#52c41a',
         borderColor: '#52c41a',
         borderRadius: '8px'
        }}
       >
        {isEditMode ? 'Update Knowledge Unit' : 'Create Knowledge Unit'}
       </Button>
      </div>
     </Form.Item>
    </Form>
   </Card>

   {/* Add Existing Document Modal - Only in Edit Mode */}
   {isEditMode && (
    <Modal
     title="Add Existing Document"
     open={isAddDocumentModalVisible}
     onCancel={() => setIsAddDocumentModalVisible(false)}
     footer={null}
     width={600}
    >
     <List
      dataSource={getAvailableDocuments()}
      renderItem={(doc) => (
       <List.Item
        actions={[
         <Button
          type="primary"
          onClick={() => onAddExistingDocument(doc.id!)}
          style={{ backgroundColor: '#52c41a', borderColor: '#52c41a' }}
         >
          Add
         </Button>
        ]}
       >
        <List.Item.Meta
         title={doc.title}
         description={doc.content?.substring(0, 150) + (doc.content && doc.content.length > 150 ? '...' : '')}
        />
       </List.Item>
      )}
     />
    </Modal>
   )}

   {/* Create New Document Modal */}
   <Modal
    title="Create New Document"
    open={isNewDocumentModalVisible}
    onCancel={() => {
     setIsNewDocumentModalVisible(false);
     newDocForm.resetFields();
    }}
    footer={null}
    width={600}
   >
    <Form
     form={newDocForm}
     layout="vertical"
     onFinish={onAddNewDocument}
    >
     <Form.Item
      label="Document Name"
      name="name"
      rules={[{ required: true, message: 'Please enter document name' }]}
     >
      <Input placeholder="Enter document name" />
     </Form.Item>

     <Form.Item
      label="Content"
      name="content"
      rules={[{ required: true, message: 'Please enter document content' }]}
     >
      <TextArea rows={8} placeholder="Enter document content" />
     </Form.Item>

     <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
      <Button
       style={{ marginRight: '8px' }}
       onClick={() => {
        setIsNewDocumentModalVisible(false);
        newDocForm.resetFields();
       }}
      >
       Cancel
      </Button>
      <Button
       type="primary"
       htmlType="submit"
       style={{ backgroundColor: '#52c41a', borderColor: '#52c41a' }}
      >
       Add Document
      </Button>
     </Form.Item>
    </Form>
   </Modal>
  </div>
 );
}