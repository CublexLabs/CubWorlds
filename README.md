# CubWorlds
A modern, lightweight world management plugin. Designed as a streamlined alternative to heavier systems, LiteWorlds gives you full control over custom worlds with minimal setup and great performance.

#### 🧾 Example config.yml
```
fallback-world: world_spawn
worlds:
- name: world_spawn
  type: VOID
  prevent-grief: true
  world-border:
    size: 1010
    center:
      x: 0
      z: 0
  spawn:
    x: 0.5
    y: 0
    z: -62.5
    yaw: 0
    pitch: -5.0
commands:
  version:
    aliases:
    - ver
    permissions:
    - CubWorlds.admin.*
  reload:
    aliases:
    - rl
    permissions:
    - CubWorlds.admin.*
  create:
    aliases:
    - cr
    permissions:
    - CubWorlds.admin.*.*
  delete:
    aliases:
    - del
    permissions:
    - CubWorlds.admin.*.*
  list:
    aliases:
    - ls
    permissions:
    - CubWorlds.admin.*.*
  load:
    permissions:
    - CubWorlds.admin.*.*
  unload:
    permissions:
    - CubWorlds.admin.*.*
  teleport:
    aliases:
    - tp
    permissions:
    - CubWorlds.admin.*
```

🔗 Download the JAR, drop it in /plugins, and configure via config.yml. Restart or hot-load your worlds — it just works. Let us know what you build with it!
