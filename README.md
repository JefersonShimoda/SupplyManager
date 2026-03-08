# SupplyManager

## 📌 About

SupplyManager allows you to register products and raw materials, define product compositions, and automatically calculate what and how much to produce based on current stock — using a greedy algorithm that maximizes total production value.

---

## 🖥️ Screenshots

### Products
<img width="1775" height="853" alt="Captura de tela 2026-03-08 174740" src="https://github.com/user-attachments/assets/63f52497-4b56-4b17-80c6-1c72b7246cac" />


### Raw Materials
<img width="1779" height="853" alt="Captura de tela 2026-03-08 174900" src="https://github.com/user-attachments/assets/f06d46ea-4657-4f42-8a60-1f296fb88a82" />


### Production Suggestion
<img width="1778" height="852" alt="Captura de tela 2026-03-08 174922" src="https://github.com/user-attachments/assets/3f37add5-fc4e-4240-b7c7-e1a889e000f4" />


---

## ✨ Features

- Create, edit and delete products
- Create, edit and delete raw materials (with code, name and stock quantity)
- Associate raw materials to a product with required quantities
- Update or remove raw material associations
- Automatic production suggestion based on current stock (greedy algorithm)
- Input validation with descriptive error responses (400, 404, 409)
- API documentation via Swagger UI

---

## ⚙️ Business Rule — Production Suggestion

Given the current stock of raw materials, the system calculates what and how much to produce to **maximize total production value**.

The algorithm works as follows:
1. Loads the current stock of all raw materials
2. Sorts products by value (highest first)
3. Calculates how many units of each product can be produced with available stock
4. Virtually consumes the stock per iteration
5. Returns an optimized production plan with quantities and total value

---

## 🛠️ Tech Stack

### Backend
| Technology | Version |
|---|---|
| Java | 25 |
| Spring Boot | 4.0.2 |
| Spring Web MVC | — |
| Spring Data JPA + Hibernate | — |
| Bean Validation | — |
| Lombok | — |
| SpringDoc OpenAPI (Swagger UI) | 2.8.6 |
| PostgreSQL (production) | — |
| MySQL (development) | — |
| H2 (tests) | — |
| Gradle | — |

### Frontend
| Technology | Version |
|---|---|
| React | 19 |
| React Router DOM | 7 |
| Vite | 7 |

### Infrastructure
- **Render** — API (Docker), managed PostgreSQL and static frontend

---

## 🌐 API Endpoints

| Group | Method | Endpoint | Description |
|---|---|---|---|
| **Products** | GET | `/api/products` | List all products |
| | POST | `/api/products` | Create a product |
| | GET | `/api/products/{id}` | Get product by ID |
| | PUT | `/api/products/{id}` | Update a product |
| | DELETE | `/api/products/{id}` | Delete a product |
| **Raw Materials** | GET | `/api/raw-materials` | List all raw materials |
| | POST | `/api/raw-materials` | Create a raw material |
| | GET | `/api/raw-materials/{id}` | Get raw material by ID |
| | PUT | `/api/raw-materials/{id}` | Update a raw material |
| | DELETE | `/api/raw-materials/{id}` | Delete a raw material |
| **Composition** | GET | `/api/products/{id}/raw-materials` | List raw materials of a product |
| | POST | `/api/products/{id}/raw-materials` | Associate raw material to a product |
| | PUT | `/api/products/{id}/raw-materials/{rmId}` | Update association |
| | DELETE | `/api/products/{id}/raw-materials/{rmId}` | Remove association |
| **Production** | GET | `/api/production/suggestion` | Get production suggestion (greedy algorithm) |

> Full interactive documentation available at `/swagger-ui.html` when running locally.

---

## ✅ Tests

Coverage across **3 layers**:

**Unit — Service** (JUnit 5 + Mockito)
- `ProductServiceTest` — 10 cases covering CRUD, not found and duplicate code scenarios
- `RawMaterialServiceTest`
- `ProductRawMaterialServiceTest`
- `ProductionServiceTest` — 5 cases covering greedy algorithm behavior (priority by value, multiple raw materials, zero stock, products without composition)

**Unit — Controller** (MockMvc + Mockito)
- `ProductControllerTest`
- `RawMaterialControllerTest`
- `ProductRawMaterialControllerTest`
- `ProductionControllerTest`

**Integration** (SpringBootTest + MockMvc + H2 in-memory)
- `ProductIntegrationTest` — full CRUD flow + HTTP validations (400, 404, 409)
- `RawMaterialIntegrationTest`
- `ProductRawMaterialIntegrationTest`
- `ProductionSuggestionIntegrationTest` — end-to-end greedy algorithm with real data in database

---

## 🏗️ Architecture

```
src/
├── controller/       # REST controllers
├── service/          # Business logic
├── repository/       # Spring Data JPA repositories
├── domain/
│   ├── model/        # JPA entities (Product, RawMaterial, ProductRawMaterial)
│   └── dto/          # Data Transfer Objects
├── exception/        # GlobalExceptionHandler + custom exceptions
└── config/           # CORS and application config
```

Three environment profiles configured via `application.yaml`:
- `dev` — MySQL
- `prod` — PostgreSQL (environment variables)
- `test` — H2 in-memory

---

## 🚀 Running Locally

### Prerequisites
- Java 25
- MySQL (used on `dev` profile)
- Node.js

### Backend

```bash
# Clone the repository
git clone https://github.com/JefersonShimoda/SupplyManager.git
cd SupplyManager

# Run with dev profile (MySQL)
./gradlew bootRun
```

### Frontend

```bash
cd frontend
npm install
npm run dev
