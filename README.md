# HabitFlow

App Android para crear hábitos, registrar cumplimientos y llevar rachas. Incluye autenticación local, recordatorios y seguimiento diario o semanal.

Desarrollada con **Kotlin**, **Jetpack Compose** y **Material 3**.

## Arquitectura

Usa **MVVM** con capas separadas:

- **UI** → pantallas Compose y ViewModels
- **Domain** → modelos de negocio
- **Data** → repositorios, Room, DataStore y lógica de datos

El flujo es unidireccional: la UI observa `StateFlow` del ViewModel, que delega en los repositorios.

**Stack:** Room · DataStore · Navigation Compose · Coroutines · AlarmManager

## Estructura de carpetas

```
app/src/main/java/com/example/habits/
├── domain/model/     # Modelos (Habit, CheckIn, UiState…)
├── data/
│   ├── local/        # Room: entities, DAOs, database
│   ├── preferences/  # Sesión con DataStore
│   ├── repository/   # AuthRepository, HabitRepository
│   ├── mapper/       # Entity → Domain
│   ├── reminder/     # Notificaciones y alarmas
│   └── util/         # Lógica auxiliar (rachas, contraseñas)
└── ui/
    ├── auth/         # Login y registro
    ├── habit/        # Home, detalle y formulario
    ├── splash/       # Pantalla inicial
    ├── components/   # Componentes reutilizables
    ├── navigation/   # NavGraph y rutas
    └── theme/        # Tema Material 3
```
