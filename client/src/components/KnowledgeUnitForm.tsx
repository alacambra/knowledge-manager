import { useState } from 'preact/hooks';
import { Button, Form, Input, Typography, message, Card } from 'antd';
import { SaveOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useLocation } from 'preact-iso';
import { KnowledgeUnitRepository2 } from '../repositories/knowledge.unit.repository2';
import type { KnowledgeUnitRequest } from '../generated/api/src';

const { Title } = Typography;
const { TextArea } = Input;

interface CreateKnowledgeUnitForm {
  onSuccess?: () => void;
}

export function KnowledgeUnitForm({ onSuccess }: CreateKnowledgeUnitForm) {
  const location = useLocation();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (values: KnowledgeUnitRequest) => {
    try {
      setLoading(true);
      await KnowledgeUnitRepository2.createKnowledgeUnit(values);
      message.success('Knowledge unit created successfully!');
      form.resetFields();
      
      if (onSuccess) {
        onSuccess();
      } else {
        location.route('/');
      }
    } catch (error) {
      console.error('Error creating knowledge unit:', error);
      message.error('Failed to create knowledge unit');
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
          Back to Knowledge Units
        </Button>
        <Title level={2}>Create New Knowledge Unit</Title>
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
              { min: 2, message: 'Name must be at least 2 characters long' },
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
              { min: 10, message: 'Description must be at least 10 characters long' },
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
                Create Knowledge Unit
              </Button>
            </div>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}