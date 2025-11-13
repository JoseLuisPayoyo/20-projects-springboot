# Proyecto 1: Gestor de Notas Personales 📝

## Objetivo
Desarrollar un **CRUD completo de notas personales** utilizando **Spring Boot**, **JPA** y **REST API**, aplicando buenas prácticas de validación y manejo de excepciones.  
Este proyecto pertenece al **Módulo 1 – CRUD básicos** de la serie de 20 proyectos Spring Boot.

---

## Requisitos del proyecto

### 1. Entidad `Note`
- **Campos obligatorios:**  
  - `id`: Long, autogenerado  
  - `title`: String, obligatorio, entre 1-255 caracteres  
  - `content`: String, obligatorio, entre 1-1000 caracteres  
  - `createdAt`: LocalDateTime, autogenerado al crear la nota  

### 2. Endpoints REST
- `POST /api/notes` → Crear nota  
- `GET /api/notes` → Listar todas las notas  
- `GET /api/notes/{id}` → Obtener nota por ID  
- `PUT /api/notes/{id}` → Actualizar nota por ID  
- `DELETE /api/notes/{id}` → Eliminar nota por ID  

### 3. Validaciones y manejo de excepciones
- Validaciones de campos obligatorios y rango de caracteres  
- Exception handling global para:  
  - Nota no encontrada (`NoteNotFoundException`)  
  - Argumentos inválidos (`IllegalArgumentException`)  
  - Errores generales (`Exception`)  

### 4. Restricciones técnicas
- No usar DTOs ni relaciones entre entidades  
- Arquitectura en capas: Controller → Service → Repository  
- Persistencia con Spring Data JPA y base de datos H2 o MySQL  
- Uso de **Lombok** para getters/setters y constructores  

---

## Reto opcional (Bonus)
1. Añadir un campo `lastModified` que se actualice automáticamente al editar la nota  
2. Añadir logging para registrar operaciones CRUD  

---


