# Как залить Windows ZIP в GitHub Release

Скрипт лежит здесь (из корня репо):

```text
dev\tools\publish_release_api.py
```

Запуск:

```powershell
cd d:\HUH\the-witcher
python dev\tools\publish_release_api.py 1.1.0
```

Если API-загрузка ~430 МБ падает по сети — **залей ZIP руками** (надёжнее):

1. Открой https://github.com/top-secret666/the-witcher/releases/tag/v1.1.0  
2. **Edit release** (карандаш)  
3. **Attach binaries** → выбери файл:

```text
d:\HUH\the-witcher\dist\The-Witcher-v1.1.0-Windows.zip
```

4. **Update release**

После этого заработает прямая ссылка:

https://github.com/top-secret666/the-witcher/releases/download/v1.1.0/The-Witcher-v1.1.0-Windows.zip
