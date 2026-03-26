# Teraria Launcher v1.0.3: A tModLoader Manager

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

***

## How to use
1. Download the app from [Releases](https://github.com/amathew4538/TerrariaLauncher/releases/latest)
2. Setup your folders like this:
   1. MacOS:
    ```bash
    .
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
    .
    └── Terraria/
        ├── TerrariaLauncher.exe
        ├── Terraria.app
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
  ### ***NOTE: Your folder names must be in camelCase***
  
  3. Run the app and click launch on the instance you want!

***

## How to build
### MacOS
1. ```git clone https://github.com/amathew4538/TerrariaLauncher.git```
2. ```cd TerrariaLauncher```
3. ```mkdir app_build app_jar```
4. ```./gradlew shadowJar```
5. ```cp app/build/libs/app-all.jar app_jar/TerrariaLauncher.jar```
6. ```bash
   jpackage --type app-image \
    --dest app_build \
    --name "TerrariaLauncher" \
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
   1. In PowerShell: ```./gradlew shadowJar```
   2. In Cmd Prompt: ```gradlew shadowJar```
5. ```copy app\build\libs\app-all.jar app_jar\TerrariaLauncher.jar```
6. ```bash
   jpackage --type app-image `
    --dest app_build `
    --name "TerrariaLauncher" `
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
