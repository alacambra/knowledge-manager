package tech.lacambra.kmanager.documents;

import tech.lacambra.kmanager.generated.jooq.tables.pojos.Documents;

public class DocumentResult extends Documents {
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