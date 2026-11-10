# Coustom Keybinds

A clean client-side Fabric mod for Minecraft 26.x that allows players to bind custom commands to single keys or modifier key combinations.

---

### Direct Download

* **Latest Release**: [Download `coustom-keybinds-2.3.4.jar`](https://github.com/bruhslow/coustom-keybinds/releases/latest)
* **Repository Build**: [`build/libs/coustom-keybinds-2.3.4.jar`](build/libs/coustom-keybinds-2.3.4.jar)

---

### Overview

| Detail | Specification |
| :--- | :--- |
| **Platform** | Fabric (Client-Side) |
| **Minecraft Version** | 26.2 (26.x) |
| **Java Version** | 25 |
| **Dependencies** | Fabric Loader (>= 0.15.0), Fabric API, Cloth Config |
| **License** | MIT |

---

### Key Features

* **Custom Command Keybinds**: Bind any Minecraft chat or server command to a keyboard key or modifier combination (e.g. `Ctrl+G`, `H`, `Alt+O`).
* **Interactive Configuration Screen**: Built-in Cloth Config graphical interface for easy in-game editing without modifying raw JSON files.
* **Conflict Prevention**: Intelligent keybind detection that warns when another action shares the same shortcut.
* **Chat Screen Safety**: Automatically suppresses execution while typing in chat or container input fields.
* **Persistent Storage**: Saves shortcuts cleanly to `config/slashkey.json`.

---

### Commands & Access

| Access | Method | Description |
| :--- | :--- | :--- |
| **Command** | `/keybinds` | Opens the graphical keybind configuration screen |
| **Mod Menu** | Config Button | Accessible via the standard Mod Menu config cog |

---

### Quick Start

1. Download **`coustom-keybinds-2.3.4.jar`** and place it in `.minecraft/mods` along with Fabric Loader, Fabric API, and Cloth Config.
2. Launch Minecraft and run `/keybinds` in chat.
3. Add a new keybind entry, specify your command (e.g. `/home` or `/spawn`), and press your desired shortcut key.
4. Save and return to the game to start using your shortcuts.

---

### Building from Source

Prerequisites: JDK 25.

```bash
# Clone the repository
git clone https://github.com/bruhslow/coustom-keybinds.git

# Build the mod archive
./gradlew build
```

Compiled JARs will be generated in `build/libs/`.

---

### License

This project is licensed under the MIT License.
