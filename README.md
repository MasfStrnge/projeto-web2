# Sistema de Gestão de Horas Acadêmicas (SGHA)

## Contexto e Objetivo

O SGHA é uma aplicação Java Web (MVC) projetada para gerenciar a submissão,
verificação de duplicidades, avaliação por comissões específicas, fluxo de recursos e
homologação de comprovantes de carga horária para discentes de Sistemas de
Informação em três categorias: Horas Complementares, Ações Extensionistas e Horas
de Estágio.

Nesta etapa, o projeto contém a modelagem inicial, a página de acesso do discente e
o fluxo de cadastro e envio de solicitações, com upload de comprovantes. Os dados
ainda ficam em memória; a avaliação pelas comissões, o fluxo de recursos e a
persistência serão implementados nas próximas etapas.

## Tecnologias

- Java (JDK 11 ou superior)
- Jakarta Servlet 6 (`@WebServlet`)
- JSP + JSTL 3.0 (`jakarta.tags.core`)
- Apache Tomcat 10.1.x (standalone)

**Planejado para as próximas entregas:** Gradle (empacotamento WAR), JPA/Hibernate,
MySQL/MariaDB e camada DAO.

## Estrutura do projeto

```
projeto-web2/
├── src/main/java/br/ufrrj/sgha/controller/   # Servlets (controladores)
├── src/main/java/br/ufrrj/sgha/model/        # Modelo de domínio
├── src/main/java/br/ufrrj/sgha/repository/   # Dados em memória
├── src/main/webapp/                          # JSPs e arquivos estáticos
├── lib/                                      # Bibliotecas JSTL (.jar)
├── build/app/                                # Saída da compilação (aplicação exploded)
└── apache-tomcat-10.1.62/                    # Servidor (não versionado)
```

## Pré-requisitos

1. JDK 11 ou superior (`java -version` e `javac -version`)
2. [Apache Tomcat 10.1.x](https://tomcat.apache.org/download-10.cgi) extraído na raiz do
   projeto como `apache-tomcat-10.1.62/` (a pasta está no `.gitignore`)

## Como executar

Na raiz do projeto:

**1. Compilar os servlets**

```bash
mkdir -p build/app/WEB-INF/classes build/app/WEB-INF/lib

javac -cp "apache-tomcat-10.1.62/lib/servlet-api.jar" \
      -d build/app/WEB-INF/classes \
      $(find src/main/java -name "*.java")
```

**2. Montar a aplicação**

```bash
cp -r src/main/webapp/* build/app/
cp lib/*.jar build/app/WEB-INF/lib/
```

**3. Publicar no Tomcat**

```bash
rm -rf apache-tomcat-10.1.62/webapps/sgha
cp -r build/app apache-tomcat-10.1.62/webapps/sgha
```

**4. Iniciar o servidor**

```bash
./apache-tomcat-10.1.62/bin/startup.sh      # Linux/macOS
apache-tomcat-10.1.62\bin\startup.bat       # Windows
```

**5. Acessar**

- Página inicial: http://localhost:8080/sgha/
- Teste do servlet: http://localhost:8080/sgha/ola

Para parar o servidor, use `shutdown.sh` (ou `shutdown.bat`) na mesma pasta `bin/`.

## Armazenamento de comprovantes

O formulário de solicitação envia arquivos PDF, PNG ou JPEG de até 5 MB para
`/discente/solicitacoes`. O servidor guarda o arquivo fora da aplicação, por padrão
em `${user.home}/sgha/uploads`, e gera um nome aleatório para não usar o nome
informado pelo navegador como caminho. O caminho salvo fica associado à solicitação
no repositório em memória.

A rota `/comprovantes` também está disponível como demonstração simples de upload e
aceita arquivos de até 10 MB.

Para escolher outro diretório, defina a propriedade Java `sgha.upload.dir` ao iniciar
o Tomcat. No PowerShell, por exemplo:

```powershell
$env:CATALINA_OPTS = "$env:CATALINA_OPTS -Dsgha.upload.dir=C:\dados\sgha"
.\apache-tomcat-10.1.62\bin\startup.bat
```

Como as solicitações ficam em memória, elas deixam de existir quando o servidor é
reiniciado.

## Configuração do VS Code

Se o VS Code não localizar as classes do Jakarta Servlet, adicione o `servlet-api.jar`
do Tomcat às bibliotecas referenciadas pelo projeto e ajuste o caminho para o da sua
máquina:

```json
"java.project.referencedLibraries": [
  "lib/*.jar",
  "<caminho-do-projeto>/apache-tomcat-10.1.62/lib/servlet-api.jar"
]
```

## Roadmap

- [ ] Entregável 1: contexto e objetivo do sistema
- [ ] Entregável 2: modelagem + projeto Java Web inicial (controladores em memória, JSPs e upload)
