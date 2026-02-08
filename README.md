# SupplyManager

Supply management and production suggestion system. Register products, raw materials, define compositions and get production suggestions based on available stock.

## Features

- Create, edit and delete products
- Create, edit and delete raw materials (with name, unit and stock quantity)
- Associate raw materials to a product with required quantities
- Update or remove raw material associations from a product
- Get a production suggestion based on current stock

## Architecture

- **Backend:** Spring Boot + Java + PostgreSQL
- **Frontend:** React + Vite

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET/POST | `/api/products` | List / create products |
| GET/PUT/DELETE | `/api/products/{id}` | Get / update / delete product |
| GET/POST | `/api/raw-materials` | List / create raw materials |
| GET/PUT/DELETE | `/api/raw-materials/{id}` | Get / update / delete raw material |
| GET/POST | `/api/products/{id}/raw-materials` | List / associate raw materials to a product |
| PUT/DELETE | `/api/products/{id}/raw-materials/{rmId}` | Update / remove association |
| GET | `/api/production/suggestion` | Production suggestion (greedy algorithm) |
=======
Full-stack supply management system with stock-based production suggestions. Built with Spring Boot, React and PostgreSQL
