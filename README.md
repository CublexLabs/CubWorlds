# KuksoWorlds
A modern, lightweight world management plugin. Designed as a streamlined alternative to heavier systems, KuksoWorlds gives you full control over custom worlds with minimal setup and great performance.

#### 🧾 Example config.yml
```yaml
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
    - kuksoworlds.admin.*
  reload:
    aliases:
    - rl
    permissions:
    - kuksoworlds.admin.*
  create:
    aliases:
    - cr
    permissions:
    - kuksoworlds.admin.*
  delete:
    aliases:
    - del
    permissions:
    - kuksoworlds.admin.*
  list:
    aliases:
    - ls
    permissions:
    - kuksoworlds.admin.*
  load:
    permissions:
    - kuksoworlds.admin.*
  unload:
    permissions:
    - kuksoworlds.admin.*
  teleport:
    aliases:
    - tp
    permissions:
    - kuksoworlds.admin.*
```

🔗 Download the JAR, drop it in /plugins, and configure via config.yml. Restart or hot-load your worlds — it just works. Let us know what you build with it!