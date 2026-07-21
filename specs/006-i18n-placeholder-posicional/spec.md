# Spec 006: Placeholders de i18n sin sustituir (`%d` → `%1$d`)

> Rama: `feature/006-i18n-placeholder-posicional` (en `feature/mejoras-mati`) · Proyecto: `allergenguard` · Estado: draft
> Bug reportado por el usuario: en la UI aparece "Todos los platos (%d)" y "Hace %d días" con el `%d` crudo.

## Contexto y objetivo
En Compose Multiplatform Resources (wasmJs), `stringResource(res, args…)` **solo sustituye placeholders
posicionales** (`%1$d`, `%2$s`, …). Los placeholders **desnudos** (`%d`, `%s` sin índice) NO se sustituyen y
salen literales — aunque se pasen los argumentos correctos desde Kotlin. El Kotlin ya pasa los args bien; el
fallo está en 6 entradas de `strings.xml` que usan `%d` desnudo.

Objetivo: alinear esas 6 claves al patrón posicional ya establecido en el repo (`%1$d`), sin tocar Kotlin.

## Alcance
- **Dentro:** cambiar `%d` → `%1$d` en 6 claves, en `values/strings.xml` (EN) y `values-es/strings.xml` (ES).
- **Fuera:** cualquier cambio en Kotlin (los call-sites ya pasan el argumento), otras claves (ya posicionales).

## Claves afectadas (ambos idiomas)
| Clave | Uso | Call-site (ya pasa arg) |
|---|---|---|
| `carta_all_dishes` | "Todos los platos (%d)" | `CartaDigitalScreen.kt:228` |
| `time_minutes_ago` | "Hace %d min" | `RecentActivityList.kt:107` |
| `time_hours_ago` | "Hace %d h" | `RecentActivityList.kt:108` |
| `time_days_ago` | "Hace %d días" | `RecentActivityList.kt:110` |
| `recipes_ingredients_count` | "%d ingredientes" | (latente, sin uso actual) |
| `menus_dishes_count` | "%d platos" | (latente, sin uso actual) |

## Criterios de aceptación (Gherkin)
```gherkin
Scenario [AC-01]: el contador de platos muestra el número
  Given la pantalla "Carta Digital" con N platos
  When  se muestra el encabezado "Todos los platos"
  Then  aparece el número N entre paréntesis, no "%d"

Scenario [AC-02]: los tiempos relativos muestran el número
  Given la lista de actividad reciente del Dashboard
  When  se muestran entradas de hace minutos/horas/días
  Then  aparece el número, no "%d"

Scenario [AC-03]: ningún recurso conserva un placeholder desnudo
  Given los strings.xml (EN y ES) del adminApp
  When  se revisan las entradas con formato
  Then  todos los placeholders son posicionales (%1$d/%2$s…), ninguno es %d/%s desnudo
```

## Notas no funcionales
- **Plataforma:** web admin (wasmJs). i18n EN+ES. Sin Firebase. Validación **visual** (es un fix de recurso;
  Compose Resources genera los accessors, no hay lógica Kotlin que testear en `commonTest`).

## Trazabilidad
| AC | Verificación | ¿Rojo antes? |
|----|--------------|--------------|
| AC-01 | Visual (Carta Digital) | n/a (visual) |
| AC-02 | Visual (Dashboard) | n/a (visual) |
| AC-03 | grep de `%d`/`%s` desnudo en strings.xml | n/a |
