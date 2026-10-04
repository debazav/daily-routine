# Rotina Diária — app Android

Versão nativa (Kotlin + Jetpack Compose) do app web da raiz deste repositório. Os dados ficam só no
aparelho (Room/SQLite) e cada rotina gera um lembrete semanal no horário de início.

- **applicationId:** `com.debazapps.routine`
- **minSdk:** 26 (Android 8.0)

## Abrir e rodar
1. Android Studio → **Open** → selecione esta pasta `android/`.
2. Espere o Gradle sincronizar (na primeira vez ele baixa Gradle e dependências).
3. Escolha um emulador ou celular e clique em **Run ▶**.

## Testes
- Unitários (validação e cálculo do próximo lembrete): `./gradlew test`
- Banco (roda no emulador/celular): `./gradlew connectedAndroidTest`

## Estrutura
```
data/       Entidade Routine, DAO, banco Room e repositório
domain/     Regras de validação e formatação de horário
reminders/  Agendamento de alarmes, notificação e reagendamento após reboot
ui/         Telas em Compose (semana, dia, formulário) e tema
```
