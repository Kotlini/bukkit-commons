# Bukkit-Commons

Lightweight shared library for minecraft plugins, config loading, json storage and safe hook handler

Then in your plugin's `build.gradle`:
```groovy
repositories {
    mavenLocal()
}

dependencies {
    implementation 'fr.kotlini:bukkit-commons:{SEE-IN-PACKAGE}'
}
```

If using shadow, relocate:
```groovy
shadowJar {
    relocate 'fr.kotlini.commons', 'your.plugin.libs.commons'
}
```

### Config

YAML config loading with error handling and enum parsing.

```java
public class ItemConfigLoader extends YamlConfigLoader {

    private Map<String, Item> items;

    public ItemConfigLoader(Plugin plugin) {
        super(plugin, "items");
    }

    @Override
    protected void loadConfig() {
        items = loadSection("items", (id, reader) -> {
            Material mat = reader.readEnum("material", Material.class);
            String name = reader.requireString("name");
            int amount = reader.readInt("amount", 1);
            return new Item(id, mat, name, amount);
        });
    }
}
```

Bad config values produce clear errors:
```
[items.yml > items.diamond-sword.material] 'DIAMAND' is not a valid Material. Expected: [DIAMOND, GOLD, ...]
```

### Storage

JSON file persistence with optional caching.

```java
JsonDataStorage<PlayerData> storage = new JsonDataStorage<>(dataFolder, PlayerData.class);
storage.start();
storage.save(playerData);

CachedDataStorage<PlayerData> cached = new CachedDataStorage<>(
    storage, executor, id -> new PlayerData(id)
);
cached.start();
PlayerData data = cached.loadOrCreate(playerId);
```

### Hooks

Safe loading of optional plugin dependencies.

```java
IEconomy economy = HookLoader.load(plugin, "Vault",
    VaultHook::new, NoOpEconomy::new);

HookRegistry hooks = new HookRegistry(plugin);
hooks.tryRegister("WorldGuard",    IRegionHook.class,     WorldGuardHook::new);
hooks.tryRegister("Vault",         IEconomyHook.class,    VaultHook::new);
hooks.tryRegister("Jobs",          IBlockBreakHook.class,  JobsHook::new);

hooks.getHooks(IRegionHook.class).forEach(h -> h.canBuild(player, loc));
```
