# Sistema de Gestão de Horas Acadêmicas (SGHA)

## Contexto e Objetivo

O SGHA é uma aplicação Java Web (MVC) projetada para gerenciar a submissão,
verificação de duplicidades, avaliação por comissões específicas, fluxo de recursos e
homologação de comprovantes de carga horária para discentes de Sistemas de
Informação em três categorias: Horas Complementares, Ações Extensionistas e Horas
de Estágio.

Nesta etapa, o projeto passa a ser empacotado com Gradle (WAR) e a persistir discentes,
solicitações e membros da comissão em um banco MySQL, usando JPA/Hibernate e
camada DAO. O fluxo de recursos ainda será implementado nas próximas etapas.

## Tecnologias

- Java (JDK 11 ou superior)
- Jakarta Servlet 6 (`@WebServlet`)
- JSP + JSTL 3.0 (`jakarta.tags.core`)
- JPA 3 com Hibernate 6.4
- MySQL 8
- Gradle 8.14 (via Gradle Wrapper), empacotamento WAR
- Apache Tomcat 10.1.x (standalone)

## Estrutura do projeto

```
projeto-web2/
├── build.gradle, settings.gradle             # Build (WAR) e dependências
├── gradlew, gradlew.bat, gradle/             # Gradle Wrapper
├── db/criar-banco.sql                        # Cria o banco e o usuário no MySQL
├── src/main/java/br/ufrrj/sgha/config/       # Conexão JPA e dados iniciais
├── src/main/java/br/ufrrj/sgha/controller/   # Servlets (controladores)
├── src/main/java/br/ufrrj/sgha/dao/          # Acesso ao banco
├── src/main/java/br/ufrrj/sgha/model/        # Modelo de domínio (entidades JPA)
├── src/main/resources/META-INF/              # persistence.xml
├── src/main/webapp/                          # JSPs e arquivos estáticos
├── build/libs/sgha.war                       # WAR gerado pelo Gradle
└── apache-tomcat-10.1.62/                    # Servidor (não versionado)
```

## Pré-requisitos

1. JDK 11 ou superior (`java -version` e `javac -version`)
2. [Apache Tomcat 10.1.x](https://tomcat.apache.org/download-10.cgi) extraído na raiz do
   projeto como `apache-tomcat-10.1.62/` (a pasta está no `.gitignore`)
3. [MySQL 8](https://dev.mysql.com/downloads/mysql/) instalado e em execução

O Gradle não precisa ser instalado: o `gradlew` baixa a versão certa na primeira execução
(é preciso estar conectado à internet nessa primeira vez).

## Banco de dados

Com o MySQL em execução, crie o banco e o usuário do sistema com o script (ele pede a
senha do usuário `root` do MySQL):

```bash
mysql -u root -p < db/criar-banco.sql
```

Os dados de conexão padrão são: banco `sgha`, usuário `sgha`, senha `sgha123`, porta
`3306`.

As tabelas são criadas automaticamente pelo Hibernate na primeira execução
(`hibernate.hbm2ddl.auto=update`). Nessa mesma subida, se ainda não houver membros da
comissão, o sistema cadastra os 6 membros de demonstração (usuários `extensao`,
`complementar`, `estagio`, `coordenador`, `secretario` e `exmembro`, todos com a senha
`123`). O `exmembro` é um membro inativo, usado para testar o bloqueio de login.

Para usar outro banco ou outras credenciais, defina as variáveis de ambiente
`SGHA_DB_URL`, `SGHA_DB_USER` e `SGHA_DB_PASSWORD` (ou as propriedades Java
`sgha.db.url`, `sgha.db.user` e `sgha.db.password`) antes de iniciar o Tomcat. No
PowerShell, por exemplo:

```powershell
$env:SGHA_DB_URL = "jdbc:mysql://localhost:3306/sgha?useSSL=false&allowPublicKeyRetrieval=true"
$env:SGHA_DB_USER = "sgha"
$env:SGHA_DB_PASSWORD = "sgha123"
```

## Como executar

Na raiz do projeto, com o MySQL em execução, siga os comandos do seu sistema operacional.

> **Importante:** para aplicar qualquer alteração (novo endpoint, servlet alterado,
> JSP novo), o Tomcat precisa ser **parado antes de publicar** de novo. Se ele continuar
> rodando, a instância antiga segue servindo as classes antigas e os endpoints não
> atualizam. O comando `deploy` do Gradle gera o WAR, remove a versão anterior e o cache
> de trabalho do Tomcat (`work/`) e copia o novo `sgha.war` para `webapps/`.

### Windows (PowerShell)

```powershell
$env:CATALINA_HOME = (Resolve-Path .\apache-tomcat-10.1.62).Path
$env:CATALINA_BASE = $env:CATALINA_HOME

# 0. Parar o Tomcat, se estiver rodando
& ".\apache-tomcat-10.1.62\bin\shutdown.bat" 2>$null
Start-Sleep -Seconds 3

# 1. Gerar o WAR e publicar no Tomcat
.\gradlew.bat deploy
if ($LASTEXITCODE -ne 0) { throw "O build falhou." }

# 2. Iniciar o servidor
& ".\apache-tomcat-10.1.62\bin\startup.bat"
```

O Tomcat precisa encontrar o JDK pela variável `JAVA_HOME` (ou `JRE_HOME`). Se ainda
não estiver definida, configure `JAVA_HOME` para a pasta do JDK antes de iniciar o
servidor. Por exemplo: `$env:JAVA_HOME = "C:\Program Files\Java\jdk-20"`.

### Linux/macOS (Bash ou Zsh)

O bloco abaixo pode ser colado direto no terminal. Os comandos são encadeados com `&&`,
então, se algum passo falhar (por exemplo, a compilação), os seguintes não são
executados e o erro aparece na tela.

```bash
TOMCAT=apache-tomcat-10.1.62

{ $TOMCAT/bin/shutdown.sh 2>/dev/null; sleep 2; true; } \
&& ./gradlew deploy \
&& $TOMCAT/bin/startup.sh
```

O que cada parte faz, em ordem: para o Tomcat se estiver rodando, compila, gera o
`build/libs/sgha.war` e o publica em `webapps/`, e inicia o servidor. Se o Tomcat estiver
em outra pasta, use `./gradlew deploy -PtomcatHome=/caminho/do/tomcat`. Para só gerar o
WAR, sem publicar, use `./gradlew war`.

### Acessar

- Página inicial: http://localhost:8080/sgha/

Para parar o servidor, use `shutdown.bat` no Windows ou `shutdown.sh` no Linux/macOS,
na pasta `bin/` do Tomcat.

### Se os endpoints não atualizarem

1. Confirme que não ficou um Tomcat antigo rodando (`ps aux | grep tomcat` no
   Linux/macOS, ou o Gerenciador de Tarefas no Windows) e que a porta 8080 não está
   ocupada por ele.
2. Confira se o `gradlew deploy` terminou sem erros: se o build falha, nada é publicado.
3. Verifique o log em `apache-tomcat-10.1.62/logs/` (`catalina.out` ou
   `catalina.<data>.log`): procure por `ClassNotFoundException`,
   `UnsupportedClassVersionError` (compilado com JDK mais novo que o usado pelo
   Tomcat) e mensagens de deploy da aplicação `sgha`.
4. Servlets devem importar `jakarta.servlet.*` (Tomcat 10), nunca `javax.servlet.*`.
5. Se o log mostrar "não foi possível abrir a conexão com o banco de dados", confira se o
   MySQL está em execução, se o banco `sgha` foi criado (`db/criar-banco.sql`) e se as
   credenciais estão corretas.

## Armazenamento de comprovantes

Os comprovantes enviados pelo discente (PDF, PNG ou JPEG, até 5 MB) ficam fora da
aplicação, por padrão em `${user.home}/sgha/uploads`, com nome aleatório. O caminho é
associado à solicitação no banco de dados.

Para usar outro diretório, defina a propriedade Java `sgha.upload.dir` ao iniciar o
Tomcat. No PowerShell, por exemplo:

    $env:CATALINA_OPTS = "$env:CATALINA_OPTS -Dsgha.upload.dir=C:\dados\sgha"

As solicitações ficam no MySQL e permanecem depois de reiniciar o Tomcat. Os arquivos
dos comprovantes ficam na pasta de uploads, não no banco.

## Configuração do VS Code

Abra a pasta do projeto com a extensão *Extension Pack for Java*. Ela reconhece o projeto
Gradle sozinha e baixa as dependências (incluindo a Servlet API), sem precisar configurar
caminhos manualmente.

## Roadmap

- [x] Entregável 1: definição dos integrantes do grupo
- [x] Entregável 2: modelagem + projeto Java Web inicial (controladores em memória, JSPs, upload e painel da comissão)
- [ ] Entregável 3: Gradle + JPA/Hibernate com DAOs + servlets persistidos no MySQL
