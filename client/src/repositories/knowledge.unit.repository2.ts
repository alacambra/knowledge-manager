import { Configuration, KnowledgeUnit, KnowledgeUnitRequest, KnowledgeUnitResourceApi, KnowledgeUnitWithDocumentsResponse } from "../api";

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
  * Get a specific knowledge unit by ID with its documents
  */
 static async getKnowledgeUnitWithDocuments(id: string): Promise<KnowledgeUnitWithDocumentsResponse> {
  try {
   const client = this.getApiClient();
   const response: KnowledgeUnitWithDocumentsResponse = await client.knowledgeUnitsIdGet({ id });
   return response;
  } catch (error) {
   console.error('Error fetching knowledge unit:', error);
   throw new Error('Failed to fetch knowledge unit: ' + error.message);
  }
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
  */
 static async updateKnowledgeUnit(id: string, request: KnowledgeUnitRequest): Promise<void> {
  try {
   const client = this.getApiClient();
   await client.knowledgeUnitsIdPut({ id, knowledgeUnitRequest: request });
  } catch (error) {
   console.error('Error updating knowledge unit:', error);
   throw new Error('Failed to update knowledge unit: ' + error.message);
  }
 }

 /**
  * Delete a knowledge unit
  */
 static async deleteKnowledgeUnit(id: string): Promise<void> {
  try {
   const client = this.getApiClient();
   await client.knowledgeUnitsIdDelete({ id });
  } catch (error) {
   console.error('Error deleting knowledge unit:', error);
   throw new Error('Failed to delete knowledge unit: ' + error.message);
  }
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