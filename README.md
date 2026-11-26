# Sistema de Gestão de Horas Acadêmicas (SGHA)

## Contexto e Objetivo

O SGHA é uma aplicação Java Web (MVC) projetada para gerenciar a submissão,
verificação de duplicidades, avaliação por comissões específicas, fluxo de recursos e
homologação de comprovantes de carga horária para discentes de Sistemas de
Informação em três categorias: Horas Complementares, Ações Extensionistas e Horas
de Estágio.

Nesta etapa, o projeto contém a modelagem inicial, as páginas de acesso do discente e
da comissão, o cadastro e envio de solicitações com upload de comprovantes e um
painel em que a comissão registra parecer e horas concedidas. Os dados das
solicitações ainda ficam em memória. O fluxo de recursos e a persistência ainda serão
implementados nas próximas etapas.

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

Na raiz do projeto, siga os comandos do seu sistema operacional.

> **Importante:** para aplicar qualquer alteração (novo endpoint, servlet alterado,
> JSP novo), o Tomcat precisa ser **parado antes de publicar** de novo. Se ele continuar
> rodando, a instância antiga segue servindo as classes antigas e os endpoints não
> atualizam. Os comandos abaixo já param o Tomcat primeiro, e também limpam o `build/`
> e o cache de trabalho do Tomcat (`work/`), para não sobrar `.class` de servlets
> renomeados ou removidos.

### Windows (PowerShell)

```powershell
$tomcat = ".\apache-tomcat-10.1.62"
$build = ".\build\app"
$env:CATALINA_HOME = (Resolve-Path $tomcat).Path
$env:CATALINA_BASE = $env:CATALINA_HOME

# 0. Parar o Tomcat, se estiver rodando
& "$tomcat\bin\shutdown.bat" 2>$null
Start-Sleep -Seconds 3

# 1. Compilar os servlets (build limpo)
if (Test-Path ".\build") { Remove-Item ".\build" -Recurse -Force }
New-Item -ItemType Directory -Force "$build\WEB-INF\classes", "$build\WEB-INF\lib" | Out-Null
$javaFiles = Get-ChildItem ".\src\main\java" -Filter "*.java" -Recurse | ForEach-Object FullName
javac -cp "$tomcat\lib\servlet-api.jar" -d "$build\WEB-INF\classes" $javaFiles
if ($LASTEXITCODE -ne 0) { throw "A compilação falhou." }

# 2. Montar a aplicação
Copy-Item ".\src\main\webapp\*" $build -Recurse -Force
Copy-Item ".\lib\*.jar" "$build\WEB-INF\lib" -Force

# 3. Publicar no Tomcat (substitui a versão anterior e limpa o cache de trabalho)
$deploy = "$tomcat\webapps\sgha"
$work = "$tomcat\work\Catalina\localhost\sgha"
if (Test-Path $deploy) { Remove-Item $deploy -Recurse -Force }
if (Test-Path $work) { Remove-Item $work -Recurse -Force }
Copy-Item $build $deploy -Recurse

# 4. Iniciar o servidor
& "$tomcat\bin\startup.bat"
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
&& rm -rf build \
&& mkdir -p build/app/WEB-INF/classes build/app/WEB-INF/lib \
&& javac -cp "$TOMCAT/lib/servlet-api.jar" \
         -d build/app/WEB-INF/classes \
         $(find src/main/java -name "*.java") \
&& cp -r src/main/webapp/* build/app/ \
&& cp lib/*.jar build/app/WEB-INF/lib/ \
&& rm -rf $TOMCAT/webapps/sgha $TOMCAT/work/Catalina/localhost/sgha \
&& cp -r build/app $TOMCAT/webapps/sgha \
&& $TOMCAT/bin/startup.sh
```

O que cada parte faz, em ordem: para o Tomcat se estiver rodando, limpa o `build/` e cria
as pastas, compila os servlets, monta a aplicação, remove o deploy antigo e o cache
`work/`, publica a nova versão e inicia o servidor.

### Acessar

- Página inicial: http://localhost:8080/sgha/

Para parar o servidor, use `shutdown.bat` no Windows ou `shutdown.sh` no Linux/macOS,
na pasta `bin/` do Tomcat.

### Se os endpoints não atualizarem

1. Confirme que não ficou um Tomcat antigo rodando (`ps aux | grep tomcat` no
   Linux/macOS, ou o Gerenciador de Tarefas no Windows) e que a porta 8080 não está
   ocupada por ele.
2. Confira se a compilação terminou sem erros: um `javac` que falha deixa o build antigo.
3. Verifique o log em `apache-tomcat-10.1.62/logs/` (`catalina.out` ou
   `catalina.<data>.log`): procure por `ClassNotFoundException`,
   `UnsupportedClassVersionError` (compilado com JDK mais novo que o usado pelo
   Tomcat) e mensagens de deploy da aplicação `sgha`.
4. Servlets devem importar `jakarta.servlet.*` (Tomcat 10), nunca `javax.servlet.*`.

## Armazenamento de comprovantes

Os comprovantes enviados pelo discente (PDF, PNG ou JPEG, até 5 MB) ficam fora da
aplicação, por padrão em `${user.home}/sgha/uploads`, com nome aleatório. O caminho é
associado à solicitação no repositório em memória.

Para usar outro diretório, defina a propriedade Java `sgha.upload.dir` ao iniciar o
Tomcat. No PowerShell, por exemplo:

    $env:CATALINA_OPTS = "$env:CATALINA_OPTS -Dsgha.upload.dir=C:\dados\sgha"

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

- [x] Entregável 1: definição dos integrantes do grupo
- [x] Entregável 2: modelagem + projeto Java Web inicial (controladores em memória, JSPs, upload e painel da comissão)