#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FORCE=0
if [ "${1:-}" = "--force" ]; then
  FORCE=1
fi

mapfile -t SERVICES < <(find "$ROOT" -maxdepth 2 -type f -name pom.xml -printf '%h\n' | sort -u)

if [ "${#SERVICES[@]}" -eq 0 ]; then
  for d in "$ROOT"/*; do
    base="$(basename "$d")"
    case "$base" in
      *-service|api-gateway|service-discovery|auth-service|payment-service|chat-service|event-service|notification-service|participation-service)
        [ -d "$d" ] && SERVICES+=("$d")
        ;;
    esac
  done
fi

if [ "${#SERVICES[@]}" -eq 0 ]; then
  echo "Nenhum serviço detectado (procure por pom.xml ou diretórios *-service)." >&2
  exit 1
fi

DOCKERFILE_CONTENT='FROM maven:3-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN groupadd -r app && useradd -r -g app app

COPY --from=build /workspace/target/*.jar app.jar

USER app
EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV JWT_SECRET=""

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]'

DOCKERIGNORE_CONTENT='target
**/target
*.log
*.tmp
*.bak

.git
.gitignore
.vscode
.idea
.DS_Store

node_modules
npm-debug.log
yarn-error.log

Dockerfile*
*.iml
*.class
'

echo "Gerando Dockerfile e .dockerignore para ${#SERVICES[@]} serviços..."
for svc in "${SERVICES[@]}"; do
  svc_dir="$svc"
  if [ "${svc_dir:0:1}" != "/" ]; then
    svc_dir="$ROOT/$svc_dir"
  fi

  df="$svc_dir/Dockerfile"
  di="$svc_dir/.dockerignore"

  if [ -f "$df" ] && [ $FORCE -ne 1 ]; then
    echo "Pulando $svc_dir (Dockerfile existe). Use --force para sobrescrever."
  else
    echo "Criando $df"
    printf "%s\n" "$DOCKERFILE_CONTENT" > "$df"
  fi

  if [ -f "$di" ] && [ $FORCE -ne 1 ]; then
    echo "Pulando $di (já existe)."
  else
    echo "Criando $di"
    printf "%s\n" "$DOCKERIGNORE_CONTENT" > "$di"
  fi
done

echo "Pronto. Para forçar sobrescrita use: bash $(realpath "$0") --force"
chmod +x "$ROOT/scripts/generate-dockerfiles.sh" || true
