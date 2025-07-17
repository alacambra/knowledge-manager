# this file needs to be sourced, not execurted. Otherwise exports applyes only on the current scripts execution and not the current shell
PROJECT_HOME=/Users/albert/git/knowledge-manager/server/
export $(cat ${PROJECT_HOME}.env.backend  | grep -v '^#' | xargs)
export POSTGRES_JDBC_URL="jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/knowledge_manager"