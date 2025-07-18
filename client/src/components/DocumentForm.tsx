import { useState } from 'preact/hooks';
import { Button, Form, Input, Typography, Card } from 'antd';
import { SaveOutlined, ArrowLeftOutlined } from '@ant-design/icons';

const { Title } = Typography;
const { TextArea } = Input;

interface DocumentFormData {
  name: string;
  content: string;
}

interface DocumentFormProps {
  onSubmit: (values: DocumentFormData) => void;
  onCancel: () => void;
  loading: boolean;
}

export function DocumentForm({ onSubmit, onCancel, loading }: DocumentFormProps) {
  const [form] = Form.useForm();

  const handleSubmit = (values: DocumentFormData) => {
    onSubmit(values);
    form.resetFields();
  };

  return (
    <div style={{ padding: '24px', maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ marginBottom: '24px' }}>
        <Button
          type="text"
          icon={<ArrowLeftOutlined />}
          onClick={onCancel}
          style={{ marginBottom: '16px' }}
        >
          Back to Knowledge Unit
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
            label="Name"
            name="name"
            rules={[
              { required: true, message: 'Please enter a name for the document' },
              { min: 2, message: 'Name must be at least 2 characters long' },
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
                onClick={onCancel}
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