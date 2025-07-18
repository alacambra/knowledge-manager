import { Button, Form, Input, Typography, Card, List } from 'antd';
import { SaveOutlined, ArrowLeftOutlined, PlusOutlined, DeleteOutlined } from '@ant-design/icons';
import { useKnowledgeUnit } from '../hooks/useKnowledgeUnit';
import type { KnowledgeUnitRequest } from '../api/models';

const { Title } = Typography;
const { TextArea } = Input;

interface KnowledgeUnitFormProps {
  useKnowledgeUnitHook?: ReturnType<typeof useKnowledgeUnit>;
  onSuccess?: () => void;
}

export function KnowledgeUnitForm({ 
  useKnowledgeUnitHook,
  onSuccess 
}: KnowledgeUnitFormProps) {
  const [form] = Form.useForm();
  
  const defaultHook = useKnowledgeUnit({ onSuccess });
  const {
    loading,
    documents,
    handleSubmit,
    handleCancel,
    handleAddDocument,
    handleRemoveDocument
  } = useKnowledgeUnitHook || defaultHook;

  const handleFormSubmit = (values: KnowledgeUnitRequest) => {
    handleSubmit(values);
    form.resetFields();
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
        <Title level={2}>Create New Knowledge Unit</Title>
      </div>

      <Card>
        <Form
          form={form}
          layout="vertical"
          onFinish={handleFormSubmit}
          autoComplete="off"
        >
          <Form.Item
            label="Name"
            name="name"
            rules={[
              { required: true, message: 'Please enter a name for the knowledge unit' },
              // { min: 2, message: 'Name must be at least 2 characters long' },
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
              // { min: 10, message: 'Description must be at least 10 characters long' },
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

          {documents.length > 0 && (
            <Form.Item label="Documents">
              <List
                size="small"
                dataSource={documents}
                renderItem={(doc, index) => (
                  <List.Item
                    actions={[
                      <Button
                        type="text"
                        danger
                        icon={<DeleteOutlined />}
                        onClick={() => handleRemoveDocument(index)}
                      />
                    ]}
                  >
                    <List.Item.Meta
                      title={doc.name}
                      description={doc.content?.substring(0, 100) + (doc.content && doc.content.length > 100 ? '...' : '')}
                    />
                  </List.Item>
                )}
              />
            </Form.Item>
          )}

          <Form.Item style={{ marginBottom: 0 }}>
            <div style={{ display: 'flex', gap: '12px', justifyContent: 'space-between' }}>
              <Button
                type="default"
                onClick={handleAddDocument}
                icon={<PlusOutlined />}
                size="large"
                style={{ borderRadius: '8px' }}
              >
                Add Document
              </Button>
              <div style={{ display: 'flex', gap: '12px' }}>
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
                  loading={loading}
                  icon={<SaveOutlined />}
                  size="large"
                  style={{
                    backgroundColor: '#52c41a',
                    borderColor: '#52c41a',
                    borderRadius: '8px'
                  }}
                >
                  Create Knowledge Unit
                </Button>
              </div>
            </div>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}