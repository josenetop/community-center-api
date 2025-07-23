# 🏥 Community Center API

Sistema RESTful desenvolvido em **Kotlin + Spring Boot**, com **MongoDB**, para gestão de **centros comunitários** durante emergências. A API permite cadastrar, atualizar e trocar recursos entre centros, além de gerar relatórios úteis para decisões rápidas e seguras.

## 🌐 Acesso à Aplicação

A aplicação está disponível publicamente em:  
👉 [https://community.planejeisoftwarehouse.com.br/swagger-ui/index.html](https://community.planejeisoftwarehouse.com.br/swagger-ui/index.html)

Notificações em tempo real são enviadas via nosso canal Discord:  
💬 [https://discord.gg/W6z7SJW7](https://discord.gg/W6z7SJW7)

---

## 📌 Funcionalidades Implementadas

### ✅ Centros Comunitários

- [x] **Criar centro** (`POST /api/v1/community-centers`)
- [x] **Listar todos** (`GET /api/v1/community-centers`)
- [x] **Buscar por ID** (`GET /api/v1/community-centers/{id}`)
- [x] **Criar em massa** (`POST /api/v1/community-centers/bulk`)
- [x] **Atualizar ocupação** (`PATCH /api/v1/community-centers/{id}/occupation`)
    - Notificação enviada via webhook quando a ocupação atinge 100%.

### ✅ Intercâmbio de Recursos

- [x] `POST /api/v1/community-centers/exchange`
- Trocas validadas com base na tabela de pontos.
- Trocas desbalanceadas permitidas se um dos centros tiver ocupação > 90%.
- Histórico de trocas persistido.

### ✅ Relatórios (em progresso)

- [ ] Centros com ocupação > 90%
- [ ] Quantidade média de cada recurso por centro
- [ ] Histórico de negociações com filtros por centro e data

---

## 🧮 Tabela de Pontos dos Recursos

| Recurso                    | Pontos |
|---------------------------|--------|
| 1 Médico                  | 4      |
| 1 Voluntário              | 3      |
| 1 Kit de suprimentos      | 7      |
| 1 Veículo de transporte   | 5      |
| 1 Cesta básica            | 2      |

---

## 🛠️ Tecnologias Utilizadas

- ✅ Kotlin 1.9
- ✅ Spring Boot 3
- ✅ MongoDB
- ✅ Docker + Docker Compose
- ✅ Swagger/OpenAPI (via SpringDoc)
- ✅ Webhook via Discord (para notificação de ocupação máxima)

---

## 🧪 Testes

- Testes unitários implementados com:
    - `kotlin.test`
    - `spring-boot-starter-test`
- Cobertura focada nas regras de negócio, validação de trocas e ocupação.

---

## 📂 Estrutura do Projeto

```
src/
├── Application/DTO/                  # DTOs para entrada/saída
├── Domain/Model/                    # Entidades de domínio
├── Domain/Exception/               # Exceções de negócio
├── Infrastructure/Controller/      # Controllers da API REST
├── Infrastructure/Service/         # Serviços de aplicação
├── Infrastructure/Event/           # Listeners e notificações
```

---

## 🚀 Executando o Projeto com Docker

```bash
# Clonar o projeto
git clone https://github.com/josenetop/community-center-api/
cd community-center-api

# Subir com Docker Compose
docker-compose up -d --build
```

---

## 📘 Documentação da API

A documentação interativa está disponível via Swagger:

> [https://community.planejeisoftwarehouse.com.br/swagger-ui/index.html](https://community.planejeisoftwarehouse.com.br/swagger-ui/index.html)

---

## <img src="https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR8aMugg7LWDXqkWc-9JlApM4MLPXhi-EPDYA&s" alt="Discord Logo" width="24" /> Discord

🔗 Discord: [https://discord.gg/W6z7SJW7](https://discord.gg/W6z7SJW7) 

---

---

## <img src="https://www.opc-router.de/wp-content/uploads/2023/07/Docker_150x150px-01-01-01.png" alt="Docker Hub" width="24" /> Discord

🔗 DockerHub: [josenetop/community-center-api](https://hub.docker.com/r/josenetop/community-center-api)

---

## ✍️ Comentários Finais

Durante o desenvolvimento, foram aplicadas boas práticas de **Clean Architecture**, **validações robustas**, uso adequado de **exceptions personalizadas** e padronização REST. Mesmo em um cenário de urgência, o código foi estruturado visando **manutenibilidade** e **escalabilidade**.

---

> Desenvolvido por José Neto 🚀