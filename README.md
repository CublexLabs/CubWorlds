# LiteWorlds
A modern, lightweight world management plugin. Designed as a streamlined alternative to heavier systems, LiteWorlds gives you full control over custom worlds with minimal setup and great performance.

## 🚀 Key Features
```
Load any number of worlds at server startup via config.yml
Hot-load and unload worlds at runtime with /liteworld load and /liteworld unload
Create new worlds with different types: NORMAL, VOID, NETHER, END
Safely delete worlds with /liteworld delete (includes player relocation + folder cleanup)
```

### 🧭 Teleportation & Spawn Control
```
Teleport to any loaded world with /liteworld tp
Set custom spawn points per world using spawn: "x,y,z,yaw,pitch"
```

### 🔐 World Access Control
```
Define permissions required to enter worlds (e.g. liteworld.access.adventure)
Players without access are automatically teleported to a fallback world
Fallback world is configurable (defaults to "world")
```

### 🛡️ Grief Prevention
```
prevent-grief: true # it disables block breaking, placing, and interaction in that world
```

### 📃 Command System
All commands are handled via /liteworld:
Commands | Description | Permission | Group
--- | --- | --- | --- 
/liteworld create <name> <type> | Create a new world  | - | -
/liteworld tp <world> | Teleport to a world | - | -
/liteworld list | View all worlds and their status | - | -
/liteworld load <world> | Load a world from config | - | -
/liteworld unload <world> | Unload a world from memory | - | -
/liteworld delete <world> | Unload and delete world folder | - | -
---

### 🧪 Lightweight, Modular Design
Works great out of the box. 
Optional [Corlex](https://github.com/DevBD1/Corlex) support for multilingual messages (fallback to YAML if not installed)

####🧾 Example config.yml
```
fallback-world: world
worlds:
  - name: lobby
    type: VOID
    prevent-grief: true
    permission: liteworld.access.lobby
    spawn: "0.5,64,0.5,0,0"

  - name: nether_arena
    type: NETHER
    prevent-grief: false
```

#### 📌 Requirements
```
PaperMC 1.20+ (tested with 1.21.4 and 1.21.5)
Java 21+
(Optional) CorlexAPI for advanced messaging and localization
```

#### 📣 What’s next?
```
/liteworld reload command
World info viewer (/liteworld info <world>)
GUI-based world manager
Auto-unload idle worlds
Multi-world templates
```

🔗 Download the JAR, drop it in /plugins, and configure via config.yml. Restart or hot-load your worlds — it just works. Let us know what you build with it!
