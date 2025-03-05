# Feature: Authentication

## **Descripción**
Se ha implementado la autenticación en la aplicación utilizando Firebase Authentication y Firestore, además de integrar Material Design 3 para mejorar la apariencia de la UI.

## **Cambios realizados**
- Se integró **Material Design 3** en el proyecto.
- Se actualizó el tema de la app con `Theme.Material3.Light.NoActionBar`.
- Se creó la funcionalidad de **Login y Registro de usuarios** con Firebase Authentication.
- Se implementó Firestore para almacenar datos adicionales de los usuarios.
- Se manejaron excepciones específicas de Firebase (`FirebaseAuthWeakPasswordException`, `FirebaseAuthUserCollisionException`, etc.).
- Se mejoró el diseño de las pantallas de login y registro con **Material Components**.
- Se corrigieron errores en `AuthViewModel`, `AuthRepository` y `RegisterFragment`.

