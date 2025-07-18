import { Button, Form, Input, Typography, Card } from 'antd';
import { SaveOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useDocument } from '../hooks/useDocument';
import type { DocumentRequest } from '../api/models';

const { Title } = Typography;
const { TextArea } = Input;

interface DocumentFormData {
  name: string;
  content: string;
}

interface DocumentFormProps {
  useDocumentHook?: ReturnType<typeof useDocument>;
  onSuccess?: (document: DocumentRequest) => void;
  onCancel?: () => void;
  mode?: 'embedded' | 'standalone';
}

export function DocumentForm({ 
  useDocumentHook, 
  onSuccess, 
  onCancel, 
  mode = 'standalone' 
}: DocumentFormProps) {
  const [form] = Form.useForm();
  
  const defaultHook = useDocument({ 
    mode, 
    onSuccess, 
    onCancel 
  });
  
  const { loading, handleSubmit, handleCancel } = useDocumentHook || defaultHook;

  const handleFormSubmit = (values: DocumentFormData) => {
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
          {mode === 'embedded' ? 'Back to Knowledge Unit' : 'Back to Documents'}
        </Button>
        <Title level={2}>Create New Document</Title>
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
              { required: true, message: 'Please enter a name for the document' },
              // { min: 2, message: 'Name must be at least 2 characters long' },
              { max: 200, message: 'Name must not exceed 200 characters' }
            ]}
          >
            <Input
              placeholder="Enter document name"
              size="large"
              style={{ borderRadius: '8px' }}
            />
          </Form.Item>

          <Form.Item
            label="Content"
            name="content"
            rules={[
              { required: true, message: 'Please enter document content' },
              // { min: 10, message: 'Content must be at least 10 characters long' }
            ]}
          >
            <TextArea
              placeholder="Enter document content"
              rows={12}
              size="large"
              style={{ borderRadius: '8px' }}
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
                loading={loading}
                icon={<SaveOutlined />}
                size="large"
                style={{
                  backgroundColor: '#52c41a',
                  borderColor: '#52c41a',
                  borderRadius: '8px'
                }}
              >
                Add Document
              </Button>
            </div>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}