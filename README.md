# Teraria Launcher v1.3.6: A tModLoader Instance Manager

<table align="center">
  <tr style="background-color: #545454;">
    <td align="center" style="padding: 20px; border: none;">
      <h3 style="color: #ffffff;">Huge thanks to the iTerm team!</h3>
      <p style="color: #cccccc;">iTerm2 is a vital part of this launcher's MacOS compatibility.</p>
      <a href="https://www.patreon.com/gnachman">
        <img src="https://raw.githubusercontent.com/gauravghongde/social-icons/master/PNG/Color/Patreon.png" width="40px" />
        <br>
        <b style="color: #f96854;">Support George Nachman (iTerm2 Creator) on Patreon</b>
      </a>
    </td>
  </tr>
</table>

## Features

- It sits as an executable in your folder
- It automatically determines the tmodloader instance through folder name
- It can launch both Base Terraria and any tmodloader instances
- It caches loaded mods in each instance
- It currently supports MacOS
- Windows isn't supported (yet)<sup>1</sup>
- It can change active mods in an instance
- It can delete instances

***

## How to use

1. Download the app from [Releases](https://github.com/amathew4538/TerrariaLauncher/releases/latest)
2. Obtain a tmodloader ***PREVIEW*** instance from their [Github](https://github.com/tModLoader/tModLoader/releases/latest)
3. Setup your folders like this:

   ### ***NOTE: Your folder names must be in camelCase***

   ### Don't be alarmed if iTerm.app gets deleted after the first run. That is normal and it doesn't need to be replaced

    1. MacOS:

    ```bash
    ~/Applications/
    └── Terraria/
        ├── TerrariaLauncher.app
        ├── iTerm.app
        ├── Terraria.app
        ├── calamityMod/
        │   ├── start-tmodloader.sh
        │   ├── LaunchUtils/
        │   │   ├── ScriptCaller.sh
        │   │   └── ...
        │   ├── icon.png
        │   └── ...
        ├── thoriumMod/
        │   ├── start-tmodloader.sh
        │   ├── LaunchUtils/
        │   │   ├── ScriptCaller.sh
        │   │   └── ...
        │   ├── icon.png
        │   ├── enabled.json
        │   └── ...
        └── ...
    ```

    2. Windows:

    ```bash
    C:/Users/{YOUR USERNAME}/Documents/
    └── Terraria/
        ├── TerrariaLauncher.exe
        ├── runtime/
        │    └── ...
        ├── app/
        │    └── ...
        ├── Terraria.exe
        ├── calamityMod/
        │   ├── start-tmodloader.bat
        │   ├── LaunchUtils/
        │   │   ├── ScriptCaller.bat
        │   │   └── ...
        │   ├── icon.png
        │   └── ...
        ├── thoriumMod/
        │   ├── start-tmodloader.bat
        │   ├── LaunchUtils/
        │   │   ├── ScriptCaller.bat
        │   │   └── ...
        │   ├── icon.png
        │   ├── enabled.json
        │   └── ...
        └── ...
    ```

4. Add an `icon.png` to every folder (base Terraria already has one)
5. Delete steam_api.dll on Windows
6. Add ***ALL*** of your mods to the local folder
    1. MacOS
    ```~/Library/Application Support/Terraria/tModLoader-preview/Mods```
    2. Windows
    ```C:\Users\{YOUR USERNAME}\Documents\My Games\Terraria\tModLoader\Mods```
7. In each instance, enable the mods you want. (Enabled mods will be managed by the app)
8. Run the app and click launch on the instance!

***

## How to build

### MacOS

1. ```git clone https://github.com/amathew4538/TerrariaLauncher.git```
2. ```cd TerrariaLauncher```
3. ```mkdir app_build app_jar```
4. ```./gradlew shadowJar```
5. ```cp app/build/libs/app-all.jar app_jar/TerrariaLauncher.jar```
6. Copy this (change the version number if you want), then run it

   ```bash
   jpackage --type app-image \
    --dest app_build \
    --name "TerrariaLauncher" \
    --app-version 1.0.0 \
    --input app_jar/ \
    --main-jar TerrariaLauncher.jar \
    --main-class TerrariaLauncher.TerrariaLauncher \
    --icon package-resources/icon.icns
   ```

7. Move your app to the folder with your Terraria instances
8. Open the app!

### Windows

1. ```git clone https://github.com/amathew4538/TerrariaLauncher.git```
2. ```cd TerrariaLauncher```
3. ```mkdir app_build app_jar```
4. Compile the jar
   1. In PowerShell: ```.\gradlew shadowJar```
   2. In Cmd Prompt: ```gradlew shadowJar```
5. ```copy app\build\libs\app-all.jar app_jar\TerrariaLauncher.jar```
6. Copy this (change the version number if you want), then run it

    ```bash
   jpackage --type app-image `
    --dest app_build `
    --name "TerrariaLauncher" `
   --app-version 1.0.0 \
    --input app_jar/ `
    --main-jar TerrariaLauncher.jar `
    --main-class TerrariaLauncher.TerrariaLauncher `
    --icon package-resources/icon.ico `
    --win-shortcut `
    --win-menu
    ```

***

## Contributors

<a href="https://github.com/amathew4538/TerrariaLauncher/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=amathew4538/TerrariaLauncher" />
</a>

Contributor panel made with [contrib.rocks](https://contrib.rocks).

***

## Footnotes

<sup>1</sup> Testers and coders needed.

***

<div style="text-align: center">Copyright © 2026 amathew4538. Licensed under AGPLv3. Not liable for any damages caused through use.</div>
