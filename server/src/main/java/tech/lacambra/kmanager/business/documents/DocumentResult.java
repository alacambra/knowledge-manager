package tech.lacambra.kmanager.business.documents;

import tech.lacambra.kmanager.generated.jooq.tables.pojos.Document;

public class DocumentResult extends Document {
    private Double similarityScore;
    
    public DocumentResult() {
        super();
    }
    
    public Double getSimilarityScore() {
        return similarityScore;
    }
    
    public void setSimilarityScore(Double similarityScore) {
        this.similarityScore = similarityScore;
    }
}