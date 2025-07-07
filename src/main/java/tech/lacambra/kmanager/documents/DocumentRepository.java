package tech.lacambra.kmanager.documents;

import java.util.List;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.enterprise.context.ApplicationScoped;

import org.jooq.DSLContext;
import tech.lacambra.kmanager.generated.jooq.tables.pojos.Documents;
import static tech.lacambra.kmanager.generated.jooq.tables.Documents.*;

@ApplicationScoped
@Transactional
public class DocumentRepository {

    private DSLContext dsl;

    @Inject
    public DocumentRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<Documents> getAllDocuments() {
        return dsl.selectFrom(DOCUMENTS)
                .fetchInto(Documents.class);
    }
}