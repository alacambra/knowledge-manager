PROJECT_HOME=/Users/albert/git/knowledge-manager/
export $(cat ${PROJECT_HOME}.env.backend  | grep -v '^#' | xargs)
export POSTGRES_JDBC_URL="jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/knowledge_manager"