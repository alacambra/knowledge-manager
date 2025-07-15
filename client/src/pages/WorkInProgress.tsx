import { Button, Typography, Space } from 'antd';
import { ArrowLeftOutlined, ToolOutlined } from '@ant-design/icons';
import { useRoute, useLocation } from 'preact-iso';

const { Title, Text } = Typography;

export function WorkInProgress() {
  const routeInfo = useRoute();
  const location = useLocation();
  const knowledgeUnitId = routeInfo.params?.id;

  const handleBack = () => {
    location.route('/');
  };

  return (
    <div style={{ 
      padding: '24px', 
      height: '100vh', 
      display: 'flex', 
      alignItems: 'center', 
      justifyContent: 'center',
      backgroundColor: '#f8f9fa'
    }}>
      <div style={{ 
        textAlign: 'center', 
        maxWidth: '500px',
        padding: '32px',
        backgroundColor: 'white',
        borderRadius: '12px',
        boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)'
      }}>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <ToolOutlined style={{ fontSize: '64px', color: '#1890ff' }} />
          
          <Title level={2} style={{ margin: 0 }}>
            Work in Progress
          </Title>
          
          <Text type="secondary" style={{ fontSize: '16px' }}>
            This feature is currently under development
          </Text>
          
          {knowledgeUnitId && (
            <div style={{ 
              padding: '16px',
              backgroundColor: '#f0f8ff',
              borderRadius: '8px',
              border: '1px solid #d6e7ff'
            }}>
              <Text strong>Knowledge Unit ID: </Text>
              <Text code>{knowledgeUnitId}</Text>
            </div>
          )}
          
          <Button
            type="primary"
            icon={<ArrowLeftOutlined />}
            onClick={handleBack}
            style={{ 
              height: '40px',
              fontSize: '16px',
              borderRadius: '8px'
            }}
          >
            Back to Knowledge Units
          </Button>
        </Space>
      </div>
    </div>
  );
}