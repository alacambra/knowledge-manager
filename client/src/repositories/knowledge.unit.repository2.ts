import { KnowledgeUnitResourceApi, Configuration } from '../generated/api/src';
import type { KnowledgeUnit, KnowledgeUnitRequest } from '../generated/api/src';

/**
 * KnowledgeUnitRepository2 - Real API implementation using generated client
 * 
 * This repository uses the generated TypeScript client to interact with the 
 * Knowledge Manager API backend.
 */
export class KnowledgeUnitRepository2 {
  private static apiClient: KnowledgeUnitResourceApi;

  /**
   * Initialize the API client with configuration
   */
  private static getApiClient(): KnowledgeUnitResourceApi {
    if (!this.apiClient) {
      const config = new Configuration({
        basePath: 'http://localhost:8080',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      this.apiClient = new KnowledgeUnitResourceApi(config);
    }
    return this.apiClient;
  }

  /**
   * Get all knowledge units from the API
   */
  static async listKnowledgeUnits(): Promise<KnowledgeUnit[]> {
    try {
      const client = this.getApiClient();
      return await client.knowledgeUnitsGet();
    } catch (error) {
      console.error('Error fetching knowledge units:', error);
      throw new Error('Failed to fetch knowledge units: ' + error.message);
    }
  }

  /**
   * Get a specific knowledge unit by ID
   * Note: This method is not available in the current API spec
   */
  static async getKnowledgeUnit(id: string): Promise<KnowledgeUnit | null> {
    // This endpoint is not available in the current OpenAPI spec
    // You would need to add it to the backend API
    console.warn('getKnowledgeUnit is not implemented in the current API');
    return null;
  }

  /**
   * Create a new knowledge unit
   */
  static async createKnowledgeUnit(request: KnowledgeUnitRequest): Promise<void> {
    try {
      const client = this.getApiClient();
      await client.knowledgeUnitsPost({ knowledgeUnitRequest: request });
    } catch (error) {
      console.error('Error creating knowledge unit:', error);
      throw new Error('Failed to create knowledge unit');
    }
  }

  /**
   * Update an existing knowledge unit
   * Note: This method is not available in the current API spec
   */
  static async updateKnowledgeUnit(id: string, request: KnowledgeUnitRequest): Promise<KnowledgeUnit | null> {
    // This endpoint is not available in the current OpenAPI spec
    // You would need to add PUT /knowledge-units/{id} to the backend API
    console.warn('updateKnowledgeUnit is not implemented in the current API');
    return null;
  }

  /**
   * Delete a knowledge unit
   * Note: This method is not available in the current API spec
   */
  static async deleteKnowledgeUnit(id: string): Promise<boolean> {
    // This endpoint is not available in the current OpenAPI spec
    // You would need to add DELETE /knowledge-units/{id} to the backend API
    console.warn('deleteKnowledgeUnit is not implemented in the current API');
    return false;
  }

  /**
   * Configure the API client with custom settings
   */
  static configure(config: {
    basePath?: string;
    headers?: Record<string, string>;
  }): void {
    const configuration = new Configuration({
      basePath: config.basePath || 'http://localhost:8080',
      headers: {
        'Content-Type': 'application/json',
        ...config.headers,
      },
    });
    this.apiClient = new KnowledgeUnitResourceApi(configuration);
  }
}