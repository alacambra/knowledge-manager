/* tslint:disable */
/* eslint-disable */
/**
 * Knowledge Manager API
 * The Knowledge Manager API provides endpoints for managing knowledge units. A knowledge unit aggregates multi-dimensional information needed for specific tasks, enabling cross-domain information reuse and contextual knowledge assembly.
 *
 * The version of the OpenAPI document: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

import { mapValues } from '../runtime';
/**
 * 
 * @export
 * @interface KnowledgeUnitResource
 */
export interface KnowledgeUnitResource {
    /**
     * 
     * @type {string}
     * @memberof KnowledgeUnitResource
     */
    id?: string;
    /**
     * 
     * @type {string}
     * @memberof KnowledgeUnitResource
     */
    name: string;
    /**
     * 
     * @type {string}
     * @memberof KnowledgeUnitResource
     */
    description?: string;
    /**
     * 
     * @type {Date}
     * @memberof KnowledgeUnitResource
     */
    createdAt?: Date;
    /**
     * 
     * @type {Date}
     * @memberof KnowledgeUnitResource
     */
    updatedAt?: Date;
}

/**
 * Check if a given object implements the KnowledgeUnitResource interface.
 */
export function instanceOfKnowledgeUnitResource(value: object): value is KnowledgeUnitResource {
    if (!('name' in value) || value['name'] === undefined) return false;
    return true;
}

export function KnowledgeUnitResourceFromJSON(json: any): KnowledgeUnitResource {
    return KnowledgeUnitResourceFromJSONTyped(json, false);
}

export function KnowledgeUnitResourceFromJSONTyped(json: any, ignoreDiscriminator: boolean): KnowledgeUnitResource {
    if (json == null) {
        return json;
    }
    return {
        
        'id': json['id'] == null ? undefined : json['id'],
        'name': json['name'],
        'description': json['description'] == null ? undefined : json['description'],
        'createdAt': json['createdAt'] == null ? undefined : (new Date(json['createdAt'])),
        'updatedAt': json['updatedAt'] == null ? undefined : (new Date(json['updatedAt'])),
    };
}

export function KnowledgeUnitResourceToJSON(json: any): KnowledgeUnitResource {
    return KnowledgeUnitResourceToJSONTyped(json, false);
}

export function KnowledgeUnitResourceToJSONTyped(value?: KnowledgeUnitResource | null, ignoreDiscriminator: boolean = false): any {
    if (value == null) {
        return value;
    }

    return {
        
        'id': value['id'],
        'name': value['name'],
        'description': value['description'],
        'createdAt': value['createdAt'] == null ? undefined : ((value['createdAt']).toISOString()),
        'updatedAt': value['updatedAt'] == null ? undefined : ((value['updatedAt']).toISOString()),
    };
}

