# 🦜 Bem-Te-Vi: Sistema de Prevenção e Alerta de Incêndios Florestais

> **Bem-Te-Vi** é uma plataforma inteligente e de tempo real para monitoramento, análise espacial e alerta preventivo de focos de incêndio florestal no estado de Minas Gerais. O projeto combina inteligência geográfica (PostGIS), coleta automatizada de dados oficiais (INPE), serviços de geocodificação (LocationIQ) e alertas diretos via e-mail para proteger comunidades e ecossistemas em risco iminente.

---

## 🗺️ Visão Geral do Sistema

O sistema é batizado em homenagem ao **Bem-Te-Vi**, pássaro brasileiro conhecido por seu canto estridente e comportamento sentinela de alerta. 

A solução monitora dados de satélite fornecidos pelo INPE, filtra espacialmente os focos ativos dentro do estado de Minas Gerais e calcula se algum usuário cadastrado está dentro do raio de perigo de um incêndio (raio padrão de 3km). Em caso positivo, o sistema gera automaticamente alertas e despacha e-mails críticos de emergência em segundos.

```mermaid
graph TD
    %% Fontes de Dados e Coleta
    INPE[INPE GeoServer WFS API] -->|Dados de Focos de 48h| PyPipeline[Python Data Pipeline /dados]
    MG_Buf[(MG_Municipios_2025.shp)] -->|Filtro Espacial Geográfico| PyPipeline
    
    %% Persistência e Processamento Espacial
    PyPipeline -->|Inserção com ST_GeomFromText| DB[(PostgreSQL + PostGIS)]
    
    %% Backend
    Spring[Spring Boot API /backend] -->|Consulta Geocodificação| LocIQ[LocationIQ API]
    UserReg[Cadastro do Usuário] --> Spring
    Spring -->|Salva Lat/Lng do Endereço| DB
    
    %% Triggers e Alertas
    DB -->|Trigger Espacial ST_DWithin| Trigger[Associação de Foco ao Usuário]
    Trigger -->|Gera Alerta PENDING| DB
    
    %% Disparo de Alertas
    Spring -->|@Scheduled Worker a cada 20s| DB
    Spring -->|Disparo SMTP / Gmail| Email[E-mail de Alerta Crítico ao Usuário]
    
    %% Frontend
    Mobile[App Mobile Expo /frontend] -->|Consome APIs REST| Spring
    Mobile -->|Visualiza Focos no Mapa| DB
```

---

## 🌟 Principais Funcionalidades

1. **Pipeline de Ingestão de Dados (`/dados`)**:
   - Coleta automatizada e contínua (intervalo padrão de 30 minutos) de dados de focos ativos nas últimas 48h da API WFS do GeoServer do **INPE**.
   - Tratamento de dados geográficos com `pandas` e `geopandas`.
   - Limpeza espacial usando o Shapefile oficial dos municípios de Minas Gerais (`MG_Municipios_2025.shp`), garantindo que apenas focos no estado sejam analisados.
   
2. **Cálculo Espacial e Triggers (Banco de Dados)**:
   - Armazenamento em banco relacional **Postgres** habilitado com a extensão espacial **PostGIS**.
   - Associação geográfica automatizada: quando um foco é inserido, um gatilho de banco (Trigger SQL) calcula cruzamentos de distância (`ST_DWithin`) entre as coordenadas da casa do usuário e o raio de risco do incêndio (default de 3.000 metros).
   - Geração imediata de registros de alertas na tabela `alert` com status `PENDING`.

3. **Backend e Despacho de Notificações (`/backend`)**:
   - API robusta desenvolvida em **Java 17 / Spring Boot 4.x**.
   - Segurança robusta com **Spring Security** e tokens **JWT** (JSON Web Token).
   - Integração com a API do **LocationIQ** para geocodificação em tempo real: transforma o endereço do usuário (Rua, Número, Bairro, Cidade, Estado) em coordenadas geográficas precisas no momento do cadastro.
   - Tarefa agendada (`@Scheduled` a cada 20s) que busca alertas `PENDING` e despacha e-mails assíncronos e imediatos usando **Spring Mail / SMTP**.

4. **Aplicativo Mobile Multplataforma (`/frontend`)**:
   - Construído com **Expo** e **React Native** usando **TypeScript**.
   - Estilização moderna e fluida com **NativeWind** (Tailwind CSS adaptado para mobile).
   - Roteamento moderno baseado em arquivos utilizando **Expo Router**.
   - Fluxo completo de autenticação (Login, Cadastro de Usuário e Endereço, Recuperação de Senha).
   - **Mapa Interativo**: Tela dedicada para visualização em mapa de todos os focos de incêndio ativos em sua proximidade.
   - Dashboard administrativo para gerentes do sistema monitorarem estatísticas em tempo real.

---

## 📂 Estrutura do Projeto

O repositório é organizado de forma modular, dividindo as responsabilidades do sistema:

```text
zettaFire/
├── backend/            # API REST construída em Java / Spring Boot
│   ├── src/
│   │   └── main/java/com/br/zetta/fire/
│   │       ├── controller/     # Endpoints HTTP da API
│   │       ├── data/           # Entidades JPA (User, Address, Alert, FireEvent) e DTOs
│   │       ├── exceptions/     # Tratamento global de erros
│   │       ├── infra/security/ # Configurações de segurança JWT
│   │       ├── repository/     # Interfaces de acesso ao banco (Spring Data JPA)
│   │       └── service/        # Regras de negócio (Email, Geocoding, AlertService)
│   ├── Dockerfile
│   └── pom.xml
│
├── dados/              # Data Pipeline ETL em Python
│   ├── banco/          # Módulo de conexão e inserção no banco com psycopg2
│   ├── coleta/         # Script de requisição ao GeoServer do INPE
│   ├── tratamento/     # Script de filtragem espacial (GeoPandas + Shapefiles)
│   ├── main.py         # Arquivo de execução principal do pipeline
│   ├── Dockerfile
│   └── requirements.txt
│
├── frontend/           # App Mobile Expo / React Native
│   ├── app/            # Telas estruturadas com Expo Router (tabs, auth, admin)
│   ├── components/     # Componentes visuais reaproveitáveis
│   ├── theme/          # Variáveis de cores e tipografia
│   ├── tailwind.config.js
│   └── package.json
│
├── infra/              # Infraestrutura local e Docker Compose
│   ├── docker-compose.yml
│   └── .env.example
```

---

## 🛠️ Tecnologias Utilizadas

### Backend
* **Java 17** & **Spring Boot 4.0.3**
* **Spring Data JPA** & **Hibernate**
* **Spring Security** & **Java JWT (Auth0)**
* **Spring Mail** (SMTP / Gmail Integration)
* **Maven** (Gerenciador de dependências)

### Frontend
* **React Native** & **Expo 54**
* **TypeScript**
* **Expo Router** (Roteamento declarativo)
* **NativeWind** (Tailwind CSS para interfaces móveis)
* **React Native Maps** (Visualização geográfica)

### Data Pipeline (Dados)
* **Python 3**
* **Pandas** & **GeoPandas** (Tratamento espacial de vetores)
* **Psycopg2** (Driver Postgres para Python)
* **Requests** (Consumo da API WFS GeoServer)

### Banco de Dados & Infraestrutura
* **PostgreSQL 15** + **PostGIS 3.3**
* **Docker** & **Docker Compose**
* **LocationIQ API** (Serviço externo de geocodificação)

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
* **Docker** e **Docker Compose** instalados na máquina.
* **Node.js** instalado (para executar o app mobile localmente se desejar).
* Chave de API do **LocationIQ** (gratuita para testes).
* Credenciais de e-mail SMTP (como uma *senha de app* do Gmail) para envio de alertas.

### Passo 1: Configuração das Variáveis de Ambiente
Na pasta `/infra`, copie o arquivo `.env.example` criando um arquivo `.env`:
```bash
cp infra/.env.example infra/.env
```
Preencha as variáveis com suas configurações:
```env
# Banco de Dados
POSTGRES_USER=postZetta
POSTGRES_PASSWORD=squadA
POSTGRES_DB=zettaFire
DB_PORT=5432

# Portas dos Serviços
API_PORT=8080

# Configurações do Pipeline
PIPELINE_INTERVAL=1800  # Intervalo de execução em segundos (ex: 30 minutos)
```

No diretório `/backend/src/main/resources/application-dev.properties` (ou usando variáveis de ambiente do sistema), configure a chave do LocationIQ e dados do e-mail:
```properties
locationiq.api.key=SUA_CHAVE_LOCATIONIQ_AQUI
spring.mail.username=seu_email@gmail.com
spring.mail.password=sua_senha_de_aplicativo_aqui
JWT_SECRET=sua_chave_secreta_jwt_aqui
```

### Passo 2: Inicializando a Infraestrutura (DB, API e Pipeline)
Na raiz do projeto, acesse a pasta `infra` e suba os containers Docker:
```bash
cd infra
docker-compose up --build
```
Esse comando irá inicializar:
1. **`db` (zetta_db)**: O Postgres com PostGIS mapeado na porta `5432`.
2. **`api` (zetta_api)**: O backend Spring Boot que aguarda a saúde do banco para iniciar na porta `8080`.
3. **`pipeline` (zetta_pipeline)**: O container Python que aguarda a inicialização da API e executa o script de ingestão e inserção periódica de focos de incêndio.

### Passo 3: Executando o Aplicativo Mobile
Com os containers de infraestrutura rodando, navegue até a pasta `frontend` para iniciar o Expo:
```bash
# Entre na pasta do frontend
cd ../frontend

# Instale as dependências
npm install

# Inicie o servidor de desenvolvimento do Expo
npm run start
```
No console do Expo, você poderá:
* Pressionar `a` para abrir no emulador **Android**.
* Pressionar `i` para abrir no simulador **iOS**.
* Escanear o **QR Code** exibido usando o app **Expo Go** no seu celular físico (verifique se o celular e o computador estão conectados na mesma rede Wi-Fi).

---

## 🔒 Segurança e Segurança do Trabalho
O aplicativo possui controle de acessos em dois níveis:
* **Usuário Comum (`ROLE_USER`)**: Cadastra-se, cadastra sua residência/área de monitoramento, visualiza os focos ativos no mapa em tempo real e recebe alertas críticos se estiver em perigo.
* **Administrador (`ROLE_ADMIN`)**: Possui acesso à tela de Dashboard administrativo no aplicativo móvel para visualizar estatísticas volumétricas de focos de incêndio em Minas Gerais.

---

## ✒️ Integrantes e Créditos
Desenvolvido pelo time do **Zetta Lab** para prevenção ativa de incêndios e proteção socioambiental.

---
*Protegendo vidas e o meio ambiente com a velocidade de um canto de alerta. 🦜*
