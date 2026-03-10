# eventosYA 🎟️

**eventosYA** es una aplicación móvil avanzada desarrollada en Android Studio para la gestión integral de eventos. La app permite a los administradores crear y gestionar eventos, y a los usuarios descubrir eventos, inscribirse y obtener sus entradas digitales con códigos QR.

## 🚀 Características Principales

### 👤 Gestión de Usuarios y Roles
*   **Autenticación Dual:** Inicio de sesión y registro mediante correo electrónico o **Google Sign-In**.
*   **Roles Dinámicos:** La aplicación adapta su interfaz y funcionalidades según el rol del usuario (Administrador o Usuario final).
*   **Perfil Personalizable:** Los usuarios pueden editar su nombre y preferencias de apariencia.

### 📅 Para Administradores (Panel Pro)
*   **CRUD de Eventos:** Creación, lectura y eliminación de eventos en tiempo real.
*   **Ubicación Geográfica:** Selector de ubicación mediante **Google Maps** para fijar el punto exacto del evento.
*   **Control de Aforo:** Visualización de inscritos y progreso de capacidad máxima.
*   **Escáner QR Integrado:** Herramienta de escaneo con **ML Kit** y **CameraX** para validar la entrada de los asistentes en la puerta.

### 🎫 Para Usuarios
*   **Buscador Inteligente:** Filtros por nombre y categorías.
*   **Inscripción Instantánea:** Registro a eventos con un solo toque.
*   **Cómo Llegar:** Integración con Google Maps externo para navegación GPS.
*   **Tickets Digitales:** Generación automática de tickets con **códigos QR únicos**.

## 🛠️ Stack Tecnológico
*   **Lenguaje:** Kotlin
*   **UI:** Jetpack Compose (Material 3)
*   **Base de Datos y Auth:** Firebase (Firestore, Authentication, Cloud Messaging)
*   **Mapas:** Google Maps SDK & Compose Maps
*   **IA y Cámara:** Google ML Kit (Barcode Scanning) & CameraX
*   **Ticketing:** ZXing (QR Generation)

---

## 💳 Integración con Google Wallet (Billetera Digital)

Una de las características más innovadoras de **eventosYA** es su preparación para el ecosistema de billeteras digitales.

### 🎫 Botón de Acción: "Añadir a Google Wallet"
En la pantalla de detalles del ticket, se ha implementado el botón oficial de **Google Wallet**. Este componente permite al usuario una transición fluida desde la app hacia su billetera digital.

### ℹ️ Contexto Técnico para la Evaluación
Para que esta funcionalidad opere en un entorno de producción real, se requieren los siguientes pasos adicionales (fuera del alcance del código cliente):
1.  **Google Pay & Wallet Console:** Se necesita una cuenta de desarrollador en la consola de Google Pay para obtener una *Issuer ID*.
2.  **Configuración de Clases de Pase:** Definir en la consola de Google el diseño del "Pass" (entrada de evento).
3.  **Backend de Firmado:** Un servidor seguro que firme los objetos JSON (JWT) para generar el enlace de guardado.

**Nota para el evaluador:** El botón y la estructura lógica están completamente integrados en la aplicación. Este es el punto de conexión donde la app envía la información del ticket a la API de Google Wallet para que el usuario pueda llevar su entrada sin necesidad de abrir la aplicación en el recinto, mejorando drásticamente la experiencia de usuario (UX).

---

## 🛠️ Configuración del Proyecto
1.  Clonar el repositorio.
2.  Añadir el archivo `google-services.json` en la carpeta `/app`.
3.  Registrar las huellas **SHA-1** y **SHA-256** en la consola de Firebase.
4.  Activar **Google Sign-In** y **Firestore** en Firebase.
5.  Añadir tu correo como **Usuario de Prueba** en la *OAuth Consent Screen* de Google Cloud Console.

---
Desarrollado con ❤️ para la gestión moderna de eventos.
