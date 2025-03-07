# Feature: Authentication

## **Descripción**
Se ha implementado la autenticación en la aplicación utilizando Firebase Authentication y Firestore, además de integrar Material Design 3 para mejorar la apariencia de la UI.

## **Cambios realizados**
✅ Se integró **Material Design 3** en el proyecto.
✅ Se actualizó el tema de la app con `Theme.Material3.Light.NoActionBar`.
✅ Se creó la funcionalidad de **Login y Registro de usuarios** con Firebase Authentication.
✅ Se implementó Firestore para almacenar datos adicionales de los usuarios.
✅ Se manejaron excepciones específicas de Firebase (`FirebaseAuthWeakPasswordException`, `FirebaseAuthUserCollisionException`, etc.).
✅ Se mejoró el diseño de las pantallas de login y registro con **Material Components**.
✅ Se corrigieron errores en `AuthViewModel`, `AuthRepository` y `RegisterFragment`.

# Feature: Add Device

## 📋 **Funcionalidades implementadas**
✅ Creación de un formulario para registrar dispositivos.  
✅ Integración con **Firestore** para almacenar la información.  
✅ Cálculo automático del **monto a pagar** en base a la **frecuencia de pago** y **período seleccionado**.  
✅ Ocultación de la barra de navegación en el fragmento de registro.  
✅ Implementación de un **botón flotante (Floating Action Button - FAB)** en la pantalla **Home**, permitiendo abrir el formulario de registro rápidamente.  
✅ Redirección automática al **Home** después de guardar un dispositivo.  

---

## 🛑 **Pendientes por realizar**
❌ **Implementar validaciones** en el formulario para evitar datos incorrectos.  
❌ Manejo de errores al interactuar con Firestore.  
❌ Añadir feedback visual al usuario en caso de fallos en la conexión.  
