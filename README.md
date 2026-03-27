# Teraria Launcher v1.0.11: A tModLoader Instance Manager

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
- It currently supports Windows<sup>1</sup> and MacOS
- It ***DOES NOT*** change active mods in an instance<sup>2</sup>
- It ***DOES NOT*** delete instances<sup>2</sup>

***

## How to use
1. Download the app from [Releases](https://github.com/amathew4538/TerrariaLauncher/releases/latest)
2. Obtain a tmodloader instance from their [Github](https://github.com/tModLoader/tModLoader/releases/latest)
3. Setup your folders like this:
   ### ***NOTE: Your folder names must be in camelCase***
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
        │   └── ...
        └── ...
    ```
    2. Windows:
    ```bash
    C:/Users/{YOUR USERNAME}/Documents/
    └── Terraria/
        ├── TerrariaLauncher.exe
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
        │   └── ...
        └── ...
    ```
  4. Add an `icon.png` to every folder (base Terraria already has one)
  5. Add ***ALL*** of your mods to the local folder (Your mod folder may be different if you are using the preview)
     1. MacOS
     ```~/Library/Application Support/Terraria/tModLoader/Mods```
     2. Windows
     ```C:\Users\{YOUR USERNAME}\Documents\My Games\Terraria\tModLoader\Mods```
  6. In each instance, enable the mods you want.
  7. Run the app and click launch on the instance!

***

## How to build
### MacOS
1. ```git clone https://github.com/amathew4538/TerrariaLauncher.git```
2. ```cd TerrariaLauncher```
3. ```mkdir app_build app_jar```
4. ```./gradlew shadowJar```
5. ```cp app/build/libs/app-all.jar app_jar/TerrariaLauncher.jar```
6. Copy this and change the version number, then run it
   ```bash
   jpackage --type app-image \
    --dest app_build \
    --name "TerrariaLauncher" \
    --app-version {REPLACE THIS} \
    --input app_jar/ \
    --main-jar TerrariaLauncher.jar \
    --main-class TerrariaLauncher.TerrariaLauncher \
    --icon package-resources/icon.icns
   ```
7. Move your app to the folder with your Terraria instances
8. Open the app!
### Windows:
1. ```git clone https://github.com/amathew4538/TerrariaLauncher.git```
2. ```cd TerrariaLauncher```
3. ```mkdir app_build app_jar```
4. Compile th jar
   1. In PowerShell: ```.\gradlew shadowJar```
   2. In Cmd Prompt: ```gradlew shadowJar```
5. ```copy app\build\libs\app-all.jar app_jar\TerrariaLauncher.jar```
6. Copy this and change the version number, then run it
    ```bash
   jpackage --type app-image `
    --dest app_build `
    --name "TerrariaLauncher" `
   --app-version {REPLACE THIS} \
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

<sup>1</sup> Testers needed. Not confirmed.
<sup>2</sup> Probably coming soon.
