export interface KnowledgeUnit {
  id: string;
  name: string;
  description: string;
  createdAt: string;
  updatedAt: string;
}

export interface KnowledgeUnitRequest {
  name: string;
  description: string;
}

export class KnowledgeUnitRepository {
  private static mockData: KnowledgeUnit[] = [
    {
      id: '1',
      name: 'Payment Processing Workflow1',
      description: 'Complete workflow for processing customer payments including validation, authorization, and settlement. Covers integration with payment gateways, fraud detection, PCI compliance requirements, and handling of various payment methods including credit cards, digital wallets, and bank transfers.',
      createdAt: '2023-12-01T10:30:00Z',
      updatedAt: '2023-12-01T10:30:00Z'
    },
    {
      id: '2',
      name: 'User Authentication System',
      description: 'Authentication and authorization system architecture with JWT tokens and role-based access control. Includes password policies, multi-factor authentication, session management, OAuth integration, and security best practices for user identity management.',
      createdAt: '2023-12-02T14:15:00Z',
      updatedAt: '2023-12-02T14:15:00Z'
    },
    {
      id: '3',
      name: 'Deployment Pipeline',
      description: 'CI/CD pipeline configuration for automated testing, building, and deployment to production. Covers Docker containerization, Kubernetes orchestration, monitoring setup, rollback procedures, and infrastructure as code principles.',
      createdAt: '2023-12-03T09:45:00Z',
      updatedAt: '2023-12-03T09:45:00Z'
    },
    {
      id: '4',
      name: 'Database Migration Strategy',
      description: 'Comprehensive approach to database schema changes and data migration between environments. Includes versioning strategies, zero-downtime deployment techniques, backup and recovery procedures, and testing methodologies.',
      createdAt: '2023-12-04T16:20:00Z',
      updatedAt: '2023-12-04T16:20:00Z'
    },
    {
      id: '5',
      name: 'API Design Guidelines',
      description: 'Standards and best practices for RESTful API design including endpoint naming conventions, HTTP status codes, error handling, versioning strategies, documentation requirements, and security considerations.',
      createdAt: '2023-12-05T11:00:00Z',
      updatedAt: '2023-12-05T11:00:00Z'
    },
    {
      id: '6',
      name: 'Microservices Architecture',
      description: 'Design patterns and implementation strategies for microservices architecture including service decomposition, inter-service communication, data consistency, monitoring, and distributed system challenges.',
      createdAt: '2023-12-06T13:30:00Z',
      updatedAt: '2023-12-06T13:30:00Z'
    },
    {
      id: '7',
      name: 'Security Incident Response',
      description: 'Procedures and protocols for handling security incidents including detection, containment, eradication, recovery, and post-incident analysis. Covers communication protocols, evidence preservation, and compliance reporting.',
      createdAt: '2023-12-07T08:15:00Z',
      updatedAt: '2023-12-07T08:15:00Z'
    },
    {
      id: '8',
      name: 'Performance Optimization',
      description: 'Strategies and techniques for application performance optimization including profiling, caching strategies, database optimization, code optimization, and infrastructure scaling approaches.',
      createdAt: '2023-12-08T15:45:00Z',
      updatedAt: '2023-12-08T15:45:00Z'
    }
  ];

  static async listKnowledgeUnits(): Promise<KnowledgeUnit[]> {
    // Simulate API call delay
    await new Promise(resolve => setTimeout(resolve, 500));
    
    return Promise.resolve([...this.mockData]);
  }

  static async getKnowledgeUnit(id: string): Promise<KnowledgeUnit | null> {
    // Simulate API call delay
    await new Promise(resolve => setTimeout(resolve, 300));
    
    const unit = this.mockData.find(unit => unit.id === id);
    return Promise.resolve(unit || null);
  }

  static async createKnowledgeUnit(request: KnowledgeUnitRequest): Promise<KnowledgeUnit> {
    await new Promise(resolve => setTimeout(resolve, 100));
    
    const newUnit: KnowledgeUnit = {
      ...request,
      id: Date.now().toString(),
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    
    this.mockData.push(newUnit);
    return Promise.resolve(newUnit);
  }

  static async updateKnowledgeUnit(id: string, request: KnowledgeUnitRequest): Promise<KnowledgeUnit | null> {
    await new Promise(resolve => setTimeout(resolve, 100));
    
    const index = this.mockData.findIndex(unit => unit.id === id);
    if (index === -1) {
      return Promise.resolve(null);
    }
    
    const updatedUnit: KnowledgeUnit = {
      ...this.mockData[index],
      ...request,
      updatedAt: new Date().toISOString()
    };
    
    this.mockData[index] = updatedUnit;
    return Promise.resolve(updatedUnit);
  }

  static async deleteKnowledgeUnit(id: string): Promise<boolean> {
    // Simulate API call delay
    await new Promise(resolve => setTimeout(resolve, 100));
    
    const index = this.mockData.findIndex(unit => unit.id === id);
    if (index === -1) {
      return Promise.resolve(false);
    }
    
    this.mockData.splice(index, 1);
    return Promise.resolve(true);
  }
}