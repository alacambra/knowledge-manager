import { Configuration, KnowledgeUnit, KnowledgeUnitRequest, KnowledgeUnitResourceApi, KnowledgeUnitManagerResourceApi, KnowledgeUnitWithDocumentsResponse, KnowledgeUnitWithResourcesResponse } from "../api";

/**
 * KnowledgeUnitRepository2 - Real API implementation using generated client
 * 
 * This repository uses the generated TypeScript client to interact with the 
 * Knowledge Manager API backend.
 */
export class KnowledgeUnitRepository2 {
 private static apiClient: KnowledgeUnitResourceApi;
 private static managerApiClient: KnowledgeUnitManagerResourceApi;

 /**
  * Initialize the main API client with configuration
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
  * Initialize the manager API client for download operations
  */
 private static getManagerApiClient(): KnowledgeUnitManagerResourceApi {
  if (!this.managerApiClient) {
   const config = new Configuration({
    basePath: 'http://localhost:8080',
    headers: {
     'Content-Type': 'application/json',
    },
   });
   this.managerApiClient = new KnowledgeUnitManagerResourceApi(config);
  }
  return this.managerApiClient;
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
  * Get a specific knowledge unit by ID with its resources and document groups
  */
 static async getKnowledgeUnitWithResources(id: string): Promise<KnowledgeUnitWithResourcesResponse> {
  try {
   const client = this.getApiClient();
   const response: KnowledgeUnitWithResourcesResponse = await client.knowledgeUnitsIdGet({ id });
   return response;
  } catch (error) {
   console.error('Error fetching knowledge unit with resources:', error);
   throw new Error('Failed to fetch knowledge unit with resources: ' + error.message);
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
  * Download a knowledge unit as a file
  */
 static async downloadKnowledgeUnit(id: string): Promise<{ blob: Blob; filename: string }> {
  try {
   const client = this.getManagerApiClient();
   const response = await client.knowledgeUnitsIdDownloadGetRaw({ id });
   
   if (!response.raw.ok) {
    throw new Error(`Download failed with status ${response.raw.status}`);
   }
   
   const blob = await response.raw.blob();
   const contentDisposition = response.raw.headers.get('Content-Disposition');
   const filename = contentDisposition 
    ? contentDisposition.split('filename=')[1]?.replace(/"/g, '')
    : `knowledge_unit_${id}.txt`;
   
   return { blob, filename };
  } catch (error) {
   console.error('Error downloading knowledge unit:', error);
   throw new Error('Failed to download knowledge unit: ' + error.message);
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
  this.managerApiClient = new KnowledgeUnitManagerResourceApi(configuration);
 }
}