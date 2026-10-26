# Как загрузить каркас в GitHub через Pull Request

Репозиторий: https://github.com/alexandr1772-ops/proektnyy-praktikum

## Способ 1: через Git (Windows, macOS, Linux)

1. Скачайте ZIP-каркас и **распакуйте** его в отдельную папку.
2. В терминале выполните:

```bash
git clone https://github.com/alexandr1772-ops/proektnyy-praktikum.git
cd proektnyy-praktikum
git switch -c develop
git push -u origin develop
git switch -c feature/bootstrap
```

3. Скопируйте **содержимое распакованной папки** в клонированную папку `proektnyy-praktikum`, заменив старый README. Папку `.git` не удаляйте. Включите показ скрытых файлов, чтобы не потерять `.github/` и `.gitignore`.
4. Выполните:

```bash
git add -A
git commit -m "Add attendance tracker scaffold"
git push -u origin feature/bootstrap
```

5. Откройте [создание Pull Request](https://github.com/alexandr1772-ops/proektnyy-praktikum/compare/develop...feature/bootstrap) и убедитесь, что **base: develop**, **compare: feature/bootstrap**. Создайте PR и после проверки слейте его.
6. Создайте следующий PR: **base: main**, **compare: develop**. Это позволит перенести одобренную версию в стабильную ветку.

## Способ 2: через сайт GitHub

1. На странице репозитория откройте список веток `main` → **View all branches** → **New branch** и создайте `develop` из `main`.
2. Аналогично создайте `feature/bootstrap` из `develop`.
3. Переключитесь на `feature/bootstrap` → **Add file → Upload files**. Перетащите содержимое распакованной папки, сохранив внутренние пути `backend/`, `mobile/`, `demo/`, `docs/`, `.github/`. Если интерфейс не принял скрытую папку `.github`, добавьте YAML workflow отдельным коммитом через **Add file → Create new file** с путём `.github/workflows/backend-tests.yml`.
4. Создайте PR из `feature/bootstrap` в `develop`, затем PR из `develop` в `main`.

## Защита main

В репозитории откройте **Settings → Rules → Rulesets → New branch ruleset**. Назначьте правило для ветки `main`, установите активный режим и включите **Require a pull request before merging**. При работе командой можно требовать минимум один review; если вы единственный участник, это может заблокировать слияние, поэтому сначала согласуйте настройку с командой. Проверку статуса CI (`pytest`) добавляйте после первого успешного запуска.

## Доступ преподавателю

Для просмотра открытого репозитория достаточно отправить ссылку. Если преподаватель должен оставлять review с правами участника или редактировать код, понадобится его GitHub-логин и настройка доступа через **Settings → Collaborators and teams**.
