<div align="center">

# Six Rooms

[![License: GPLv3](https://img.shields.io/badge/license-GPLv3-blue.svg)](./LICENSE)
![Paper](https://img.shields.io/badge/Paper-1.21.8-00aaff)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.8-62b246)
![Java](https://img.shields.io/badge/Java-17+-f89820)

</div>

## Описание (RU)
Six Rooms — это мини‑игровой плагин для PaperMC, где каждый игрок находится в своей комнате и участвует в серии раундов с голосовой связью через PlasmoVoice. Раунды выбираются случайно.

### Возможности
- приватные голосовые каналы и звонки через телефон
- раунды: маски, угадывание слова, динамит, меч, двери
- очки, таймеры, подготовка между раундами
- поддержка схематики через WorldEdit и процедурных комнат
- локализация сообщений через lang.yml

### Зависимости
- PlasmoVoice (и pv-addon-groups)
- WorldEdit (для схематики комнат)

## Description (EN)
Six Rooms is a PaperMC mini‑game plugin where each player stays in a separate room and plays a sequence of rounds with voice communication through PlasmoVoice. Rounds are randomized.

### Features
- private voice channels and phone calls
- rounds: masks, word guessing, TNT, sword, doors
- scoring, timers, inter‑round preparation
- schematic rooms via WorldEdit and procedural rooms
- localization via lang.yml

### Dependencies
- PlasmoVoice (and pv-addon-groups)
- WorldEdit (for schematic rooms)

## Установка / Installation
1. Скачайте зависимости и установите их на сервер.  
2. Скопируйте jar плагина в папку plugins.  
3. Запустите сервер и настройте config.yml при необходимости.

## Команды / Commands
- /sixrooms open
- /sixrooms join
- /sixrooms leave
- /sixrooms start
- /sixrooms cancel
- /sixrooms status
- /sixrooms phone

## Права / Permissions
- sixrooms.admin

## Конфигурация / Configuration
- config.yml: language: ru | en

## Сборка / Build
```bash
./gradlew.bat build
```

## License
GPLv3. See [LICENSE](./LICENSE).
