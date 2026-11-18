# CustomServerList

A customizable entry management mod for Minecraft.

CustomServerList aims to let players and modpack creators add and manage custom entries in Minecraft's singleplayer and multiplayer menus, with flexible JSON configuration and an in-game editor.

> [!WARNING]
> **Work in Progress**
>
> CustomServerList is currently under active development. Some features described in the roadmap are not yet available.

## Features

- **In-game Configuration GUI** — Manage custom entries through the mod configuration screen.
- **JSON Configuration** — Store each entry in an independent JSON file.
- **Entry Management** — Create, edit, and delete custom entries.
- **Display Settings** — Configure whether an entry targets the singleplayer menu, multiplayer menu, or both.
- **Sorting Options** — Configure entry priority and pinned status.
- **Permission Controls** — Restrict editing or deletion through the in-game GUI.
- **Custom Actions** — Configure URL-opening actions for entries.

**Note:** The configuration interface and JSON management system are implemented, but integration with Minecraft's original world and server selection lists is still in development.

## Compatibility

| Component | Version |
|---|---|
| Minecraft | 1.21.1 |
| Mod Loader | NeoForge 21.1.x |
| Java | 21 |
| Side | Client |

Support for other Minecraft versions and mod loaders is not currently planned.

## Configuration

CustomServerList stores configuration files in:

```text
.minecraft/
└── config/
    └── customserverlist/
        └── entries/
            ├── example.json
            └── cmclauncher.json
```

Each JSON file represents a single custom entry. The filename should match the entry's ID.

### Example Configuration

```json
{
  "id": "cmclauncher",
  "name": "CMCLauncher",
  "description": "Visit the official CMCLauncher project",
  "icon": "",
  "display": "singleplayer",
  "pinned": true,
  "allowDelete": true,
  "allowEdit": true,
  "enabled": true,
  "sortOrder": 0,
  "action": "open_url",
  "url": "https://github.com/CMCLauncher-Dev/CMCLauncher"
}
```

### Configuration Reference

| Property | Type | Description |
|---|---|---|
| `id` | String | Unique identifier and JSON filename |
| `name` | String | Display name |
| `description` | String | Entry description |
| `icon` | String | Reserved for custom icon support |
| `display` | String | `singleplayer`, `multiplayer`, or `both` |
| `pinned` | Boolean | Whether the entry is pinned |
| `allowDelete` | Boolean | Whether deletion is allowed through the GUI |
| `allowEdit` | Boolean | Whether editing is allowed through the GUI |
| `enabled` | Boolean | Whether the entry is enabled |
| `sortOrder` | Integer | Sorting priority |
| `action` | String | `none` or `open_url` （Work In Process)|
| `url` | String | Target URL for the `open_url` action |

The `allowDelete` and `allowEdit` settings are GUI restrictions, not file-system security controls.

## In-game Configuration

To manage entries:

1. Launch Minecraft with CustomServerList installed.
2. Open the **Mods** menu.
3. Select **CustomServerList**.
4. Click **Config**.
5. Create, edit, or delete entries using the configuration interface.

Changes are saved to JSON files automatically when using the editor's save action.

## Development

### Requirements

- JDK 21
- Git
- A Java IDE, such as IntelliJ IDEA
- Gradle (wrapper included in the project)

### Build

Clone the repository:

```bash
git clone https://github.com/com-in/CustomServerList.git
cd CustomServerList
```

Build the mod:

**Windows**

```powershell
.\gradlew.bat build
```

**Linux / macOS**

```bash
./gradlew build
```

Build artifacts will be generated in `build/libs/` after a successful build.

### Run Development Client

**Windows**

```powershell
.\gradlew.bat runClient
```

**Linux / macOS**

```bash
./gradlew runClient
```

## Roadmap

- [x] JSON-based entry configuration
- [x] In-game entry management GUI
- [x] Mod configuration screen integration
- [x] Entry creation, editing, and deletion
- [ ] Display custom entries in the singleplayer world list
- [ ] Display custom entries in the multiplayer server list
- [ ] Custom entry icons
- [ ] Improved editor layout and field descriptions
- [ ] Localization support
- [ ] Additional entry actions
- [ ] Stable public release

## Contributing

Issues, bug reports, and feature suggestions are welcome.

If you encounter a problem, please include your Minecraft version, NeoForge version, mod version, and relevant logs when opening an issue.

## Links

- **GitHub:** https://github.com/com-in/CustomServerList
- **Issues:** https://github.com/com-in/CustomServerList/issues

## License

Please refer to the `LICENSE` file in the repository for licensing information.
