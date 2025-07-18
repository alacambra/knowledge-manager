import { Configuration, Document, DocumentRequest, DocumentsManagerResourceApi } from "../api";

/**
 * DocumentRepository - Real API implementation using generated client
 * 
 * This repository uses the generated TypeScript client to interact with the 
 * Documents Manager API backend.
 */
export class DocumentRepository {
  private static apiClient: DocumentsManagerResourceApi;

  /**
   * Initialize the API client with configuration
   */
  private static getApiClient(): DocumentsManagerResourceApi {
    if (!this.apiClient) {
      const config = new Configuration({
        basePath: 'http://localhost:8080',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      this.apiClient = new DocumentsManagerResourceApi(config);
    }
    return this.apiClient;
  }

  /**
   * Get all documents from the API
   */
  static async getAllDocuments(): Promise<Document[]> {
    try {
      const client = this.getApiClient();
      return await client.documentsGet();
    } catch (error) {
      console.error('Error fetching documents:', error);
      throw new Error('Failed to fetch documents: ' + error.message);
    }
  }

  /**
   * Upload documents to the API
   */
  static async uploadDocuments(documents: DocumentRequest[]): Promise<void> {
    try {
      const client = this.getApiClient();
      await client.documentsUploadPost({ documentRequest: documents });
    } catch (error) {
      console.error('Error uploading documents:', error);
      throw new Error('Failed to upload documents: ' + error.message);
    }
  }

  /**
   * Delete a document by ID
   */
  static async deleteDocument(id: string): Promise<void> {
    try {
      const client = this.getApiClient();
      await client.documentsIdDelete({ id });
    } catch (error) {
      console.error('Error deleting document:', error);
      throw new Error('Failed to delete document: ' + error.message);
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
    this.apiClient = new DocumentsManagerResourceApi(configuration);
  }
}