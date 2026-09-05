# Álcool ou Gasolina - Android App

O **Álcool ou Gasolina** é uma aplicação Android desenvolvida para ajudar motoristas e proprietários de veículos flex a calcularem de forma rápida e precisa qual combustível é mais vantajoso (etanol ou gasolina) com base nos preços e rendimento.

## 🚀 Tecnologias e Arquitetura

- **Linguagem:** Kotlin
- **UI:** Jetpack Compose (BOM), Material 3, Navigation Compose
- **Arquitetura:** MVVM com Unidirectional Data Flow (UDF) -> `StateFlow` + `SharedFlow`
- **Injeção de Dependências:** Koin
- **Banco de Dados Local:** Room (Flow, Coroutines) e SharedPreferences via `PreferencesRepository`
- **Async & Concorrência:** Kotlin Coroutines & Flow
- **Push & Analytics:** Firebase (Messaging, Crashlytics, Analytics)
- **Monetização:** Google AdMob (Banner, Interstitial, App Open Ads)
- **Build System:** Gradle Kotlin DSL (`build.gradle.kts`), Version Catalog (`gradle/libs.versions.toml`)
- **Qualidade & Testes:** ktlint, JUnit4, MockK, Turbine, Robolectric

## 🏗️ Estrutura do Projeto

O projeto é organizado por funcionalidades (features) e camadas:

- `application/`: Inicialização da aplicação e Koin (`CustomApplication.kt`).
- `data/`: Persistência local com Room (`AppDatabase.kt`, `ComparisonDao.kt`) e Repositórios (`data/repository/`).
- `domain/`: Entidades e modelos de domínio (`Comparison.kt`).
- `di/`: Módulos de injeção de dependência Koin (`AppModule`, `LocalModule`, `RepositoryModule`, `ViewModelModule`).
- `features/`: Módulos de telas e fluxos do app (`start`, `main`, `history`, `notifications`).
- `navigation/`: Rotas e grafo de navegação (`AppNavHost.kt`).
- `ui/`: Componentes visuais compartilhados (`ui/components/`) e temas Compose (`ui/theme/`).
- `service/`: Serviços em background como Firebase Cloud Messaging (`MyFirebaseMessagingService.kt`).
- `util/`: Utilitários gerais, máscaras e helpers.

## 📦 Comandos de Build e Teste

```bash
# Compilação do código Kotlin
./gradlew compileDebugKotlin

# Execução de testes unitários
./gradlew testDebugUnitTest

# Verificação de formatação e estilo (ktlint)
./gradlew ktlintCheck

# Auto-formatação de código
./gradlew ktlintFormat

# Geração de App Bundle para Release
./gradlew :app:bundleRelease
```
