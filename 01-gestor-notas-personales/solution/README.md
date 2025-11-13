# Solución – Proyecto 1: Gestor de Notas Personales 📝

Este directorio contiene la **implementación completa** del Proyecto 1 de la serie de 20 proyectos Spring Boot.

---

## Resumen del proyecto

El objetivo principal de este proyecto era crear un **CRUD completo de notas personales**, aplicando buenas prácticas de Spring Boot, validaciones y manejo de errores. Cada nota tiene un título, contenido y fecha de creación automática.  

Se buscó que el proyecto fuera **autocontenido**, sin relaciones ni DTOs, pero permitiendo practicar todas las bases necesarias para proyectos posteriores más complejos.

---

## Arquitectura y diseño

- **Arquitectura en capas:**  
  Se separó la lógica en **Controlador → Servicio → Repositorio → Entidad**, lo que permite un código limpio y mantenible.  

- **Validaciones:**  
  Todos los campos obligatorios se validan para evitar datos inconsistentes. Por ejemplo, título y contenido no pueden estar vacíos y deben tener un tamaño mínimo y máximo.  

- **Manejo de errores:**  
  Se implementó un sistema global de excepciones para capturar errores comunes como:  
  - Nota no encontrada  
  - Datos inválidos enviados por el cliente  
  - Errores generales del sistema  

- **Persistencia:**  
  Se utilizó Spring Data JPA con una base de datos H2 por defecto, para simplificar la ejecución y pruebas.  

- **Buenas prácticas:**  
  - Separación de capas y responsabilidades  
  - Nombres claros y consistentes  
  - Código fácilmente ampliable para proyectos posteriores  

---

## Retos y problemas comunes

Al implementar este proyecto pueden surgir varios retos que son importantes de tener en cuenta:

1. **Validaciones estrictas:**  
   Si no se manejan correctamente, los usuarios podrían enviar datos vacíos o fuera de rango, causando errores en la base de datos o respuestas inesperadas.  

2. **Manejo de excepciones:**  
   Sin un controlador global de errores, cada endpoint tendría que manejar las excepciones por separado, aumentando el código repetitivo y la posibilidad de inconsistencias.  

3. **Actualización de registros:**  
   Es importante asegurarse de que al actualizar una nota solo se modifiquen los campos permitidos y que la fecha de creación permanezca intacta.  

4. **Pruebas de API:**  
   Probar cada endpoint con datos correctos e incorrectos es crucial para asegurar que las validaciones y los mensajes de error funcionan como se espera.  

---

## Bonus implementado

- Campo opcional `lastModified` que se actualiza automáticamente cuando la nota es editada.  
- Logging básico de operaciones CRUD para facilitar la depuración y seguimiento de actividad.  

---

## Conclusión

Este proyecto sienta las bases para **CRUDs más complejos**, enseñando cómo organizar el código, validar entradas, manejar errores y trabajar con Spring Boot de forma profesional.  

A pesar de ser un proyecto sencillo, permite identificar y resolver problemas típicos en aplicaciones backend, preparando al desarrollador para los siguientes niveles de complejidad, como la integración de DTOs o relaciones entre entidades.
