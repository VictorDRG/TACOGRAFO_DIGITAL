   # 🚚 TACÓGRAFO DIGITAL - Aplicación Android 🚚 ##

Este proyecto es una aplicación móvil desarrollada para la plataforma Android que simula las funcionalidades esenciales de un tacógrafo digital, diseñada para registrar y gestionar las jornadas de actividad de un conductor.

 ## Si quieres probarlo en tu Android 12 o superior, descarga el APK de app/release/app-release-apk ##


------------------------------ ## 🌟 Características Principales 🌟 ## ------------------------------

** Control de Actividades en Tiempo Real:** Registro y visualización en vivo de los tiempos dedicados a:

     *Conducción** 🚗
     *Descanso** 😴
     *Otros Trabajos** 💼
     
** Gestión de Jornada Laboral: * Inicio y fin de la jornada con registro de duración total.

** Registro de Ubicación GPS: * Captura continua de la posición geográfica del dispositivo durante la actividad de conducción.

** Alertas de Cumplimiento Normativo:

    *Alerta de Descanso Requerido: * Notificación cuando se alcanza el límite de conducción continua.
    *Notificación de Descanso Cumplido: * Aviso cuando se ha completado el tiempo de descanso obligatorio.
    *Alerta de Fin de Jornada: * Notificación al alcanzar la duración máxima permitida de la jornada laboral.
    
** Persistencia de Datos: * Almacenamiento local del historial de todas las jornadas completadas.

** Historial de Jornadas: * Visualización detallada de todas las jornadas registradas, incluyendo tiempos y ubicaciones.

** Sistema de Login Básico: * Una pantalla de inicio de sesión simple.

------------------------------ ## 🛠️ Tecnologías y Herramientas Utilizadas 🛠️ ## ------------------------------

** Plataforma: * Android
** Lenguaje de Programación: * [Java]
** Recolección de Ubicación: * [Google Play Services Location Library](https://developers.google.com/location-history/faq)
** Persistencia Local

------------------------------ ## 📊 Funcionamiento y Recolección de Datos 📊 ## ------------------------------

La aplicación opera mediante un sistema de temporizadores que se ejecutan cada segundo, actualizando los contadores de las actividades (conducción, descanso, otros trabajos) según el estado actual seleccionado por el usuario.

** Tiempos: * Los tiempos se acumulan en variables de tipo `long` en milisegundos para garantizar precisión. La interfaz de usuario se actualiza en tiempo real para mostrar los tiempos formateados (HH:MM:SS).

** Ubicaciones: * Cuando la actividad "Conducción" está activa, la aplicación solicita actualizaciones de ubicación GPS periódicamente.

** Almacenamiento de Jornadas:

    * Cada jornada completada se encapsula en un objeto `Jornada`.
    * Este objeto, junto con la lista de ubicaciones GPS registradas durante la conducción, se convierte a un formato `JSONObject`.
    * Todas las jornadas se almacenan como un `JSONArray` en un único archivo llamado `jornadas.json` en el almacenamiento interno privado de la aplicación.
    * Al inicio de la aplicación o al acceder al historial, este archivo `jornadas.json` se lee y se reconstruyen los objetos `Jornada` en memoria.

