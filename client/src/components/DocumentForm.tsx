import { useState } from 'preact/hooks';
import { Button, Form, Input, Typography, message, Card } from 'antd';
import { SaveOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useLocation } from 'preact-iso';

const { Title } = Typography;
const { TextArea } = Input;

interface DocumentFormData {
  title: string;
  content: string;
}

interface DocumentFormProps {
  onSuccess?: () => void;
}

export function DocumentForm({ onSuccess }: DocumentFormProps) {
  const location = useLocation();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (values: DocumentFormData) => {
    try {
      setLoading(true);
      // TODO: Implement API call to create document
      console.log('Creating document:', values);
      
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      message.success('Document created successfully!');
      form.resetFields();
      
      if (onSuccess) {
        onSuccess();
      } else {
        location.route('/');
      }
    } catch (error) {
      console.error('Error creating document:', error);
      message.error('Failed to create document');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    location.route('/');
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
          Back to Documents
        </Button>
        <Title level={2}>Create New Document</Title>
      </div>

      <Card>
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          autoComplete="off"
        >
          <Form.Item
            label="Title"
            name="title"
            rules={[
              { required: true, message: 'Please enter a title for the document' },
              { min: 2, message: 'Title must be at least 2 characters long' },
              { max: 200, message: 'Title must not exceed 200 characters' }
            ]}
          >
            <Input
              placeholder="Enter document title"
              size="large"
              style={{ borderRadius: '8px' }}
            />
          </Form.Item>

          <Form.Item
            label="Content"
            name="content"
            rules={[
              { required: true, message: 'Please enter document content' },
              { min: 10, message: 'Content must be at least 10 characters long' }
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
                Create Document
              </Button>
            </div>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}