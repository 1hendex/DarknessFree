<div align="center">
  <h1>Darkness Free</h1>
  <p>Free version of Darkness Liquidation &bull; Prediction-based anti-cheat and server-protection plugin for Minecraft servers.</p>

  <p>
    <a href="https://darkness.rip">
      <img alt="Discord" src="https://img.shields.io/discord/1380822433631834122?style=flat&label=Discord&logo=discord&color=5865F2&logoColor=white">
    </a>
    <img alt="Java 17" src="https://img.shields.io/badge/Java-17%2B-ED8B00?style=flat&logo=openjdk&logoColor=white">
    <img alt="Platform Paper and Folia" src="https://img.shields.io/badge/Platform-Paper%20%2F%20Folia-FFFFFF?style=flat&logo=papermc&logoColor=black">
  </p>

  <p>
    <a href="README.md"><b>English</b></a>
    &middot;
    <a href="README-ru.md">Русский</a>
    &middot;
    <a href="README-uk.md">Українська</a>
  </p>

  <p>
    <a href="https://darkness.rip"><b>Purchase the full version at darkness.rip</b></a>
  </p>
</div>

---

### What is Darkness Free?

Darkness Free is the free version of Darkness Liquidation.
It does not include the full functionality of the plugin. Full version: [https://darkness.rip](https://darkness.rip)

Darkness is a fork of [GrimAC](https://github.com/GrimAnticheat/Grim). It builds on
GrimAC's prediction-based foundation and adds its own checks, client-protection modules,
administration tools, themes, punishments, and integrations.

### Requirements

- Java 17 or newer to run the plugin.
- A Paper-compatible server. Folia is supported.
- ~~A valid Darkness license. Set it in `plugins/Darkness/config.yml` after the first start.~~ (Not required in the free version. Full version: [https://darkness.rip](https://darkness.rip))
- JDK 21 or newer if you build the project from source.

### Installation

1. Download Darkness Free or purchase the full version at [darkness.rip](https://darkness.rip).
2. Put `Darkness.jar` into your server's `plugins` directory.
3. Start the server once to generate the configuration files.
4. Review the checks, punishments, theme, logging, and integration settings.
5. Restart the server or use `/darkness reload` after changing configuration.

### Features

- Prediction-based checks for movement, combat, reach, velocity, inventory, scaffold, timer,
  elytra, vehicles, packets, and more.
- Packet and exploit protection against invalid packets, crash attempts, suspicious client data,
  blink-like behaviour, and other protocol abuse.
- ~~Information-hiding tools that can spoof selected client-visible data and help reduce cheat-client
  advantages.~~ *(Not available in the free version. Full version: [darkness.rip](https://darkness.rip))*
- Client-brand monitoring and configurable rules for suspicious or blocked clients.
- Alerts, verbose output, player profiles, performance metrics, logs, Discord webhooks, and proxy
  alert forwarding.
- Configurable punishments, punishment waves, staff tools, themes, and English, Russian, and
  Ukrainian configuration files.
- Optional integrations with ViaVersion, ViaBackwards, Geyser, Floodgate, LuckPerms, and
  PlaceholderAPI.
- Paper and Folia support.

### Prediction engine

Darkness Liquidation is built on the prediction-based foundation of GrimAC. Instead of relying
only on simple packet limits, the engine recreates the set of movements a vanilla client could
legitimately perform and compares incoming actions against that model.

- **Vanilla movement simulation.** The engine accounts for walking, swimming, knockback, cobwebs,
  bubble columns, vehicles, elytra, and other vanilla mechanics.
- **Version-aware physics and collisions.** It handles client/server version differences, collision
  order, bounding-box changes, ViaVersion replacement blocks, and version-specific block data.
- **Per-player world replication.** The anti-cheat builds a client-view world cache from chunk,
  block-place, and block-change packets. This lets checks safely evaluate packet-side and fake
  blocks without treating them as automatic violations.
- **Latency and inventory compensation.** World updates, player states, and inventory data are
  tracked in the order they reach the client to reduce false positives caused by latency,
  ghost blocks, or desynchronisation.
- **Asynchronous architecture.** Most movement processing and listeners run around the network
  pipeline, while per-player state is designed for safe concurrent access and server scalability.
- **Security through accurate simulation.** By validating against possible vanilla behaviour,
  checks aim to make invalid movement mathematically distinguishable rather than relying on
  obscurity alone.

### Developer API

Darkness provides a plugin API for integrations with custom plugins and server systems. It can be
used to build extensions around Darkness events and functionality without depending on internal
implementation details.

- API repository and documentation: [1hendex/DarknessAPI](https://github.com/1hendex/DarknessAPI)

### Configuration

The main configuration is generated under `plugins/Darkness/`. The bundled defaults are located in
this repository:

- [`config/en.yml`](common/src/main/resources/config/en.yml) — general settings, alerts, client protection,
  Discord, logging, punishments, and integrations.
- [`checks/en.yml`](common/src/main/resources/checks/en.yml) — individual anti-cheat checks and thresholds.
- [`punishments/en.yml`](common/src/main/resources/punishments/en.yml) — violation punishment rules.
- [`themes/`](common/src/main/resources/themes) — alert and message themes.

English, Russian, and Ukrainian variants are included. Tune settings gradually and test changes on
your own server before using strict punishments in production.

### Full command list

The default primary command is `/darkness`. You can change the command names in `config.yml` with
`command_name`, for example `command_name: darkness,dl`; the first name becomes the primary command
and the remaining names are aliases. Restart the server after changing this setting. Arguments in
`<angle brackets>` are required; `[square brackets]` are optional.

| Command | Purpose |
| --- | --- |
| `/darkness` | Show plugin and version information. |
| `/darkness help` | Show the in-game help message. |
| `/darkness alerts` | Toggle violation alerts. |
| `/darkness verbose` | Toggle detailed violation output. |
| `/darkness brands` | Toggle client-brand notifications. |
| `/darkness profile <player>` | View a player's anti-cheat profile. |
| ~~/darkness topvl~~ | ~~Show players with the highest violation level.~~ |
| ~~/darkness cps <player>~~ | ~~View a player's clicks per second.~~ |
| `/darkness checks` | List available checks. |
| `/darkness checks enable <check>` | Enable a check. |
| `/darkness checks disable <check>` | Disable a check. |
| `/darkness debug [player]` | Toggle player debug output. |
| ~~/darkness consoledebug <player>~~ | ~~Toggle console debug output for a player.~~ |
| `/darkness perf` or `/darkness performance` | Toggle performance metrics. |
| ~~/darkness lag~~ | ~~Show server resource and lag information.~~ |
| ~~/darkness menu~~ | ~~Open the main Darkness GUI.~~ |
| `/darkness reload` | Reload the Darkness configuration. |
| `/darkness sendalert <message>` | Send a custom staff alert. |
| ~~/darkness bot [player]~~ | ~~Spawn a test bot around yourself or a selected player.~~ |
| `/darkness spectate <player>` | Start spectating a player. |
| `/darkness stopspectating [here]` | Stop spectating; `here` returns you at the current location when permitted. |
| ~~/darkness knockback <player> [back]~~ or ~~/darkness kb <player> [back]~~ | ~~Run a knockback test.~~ |
| ~~/darkness totem <player>~~ | ~~Run the AutoTotem check.~~ |
| ~~/darkness rotate <player>~~ | ~~Rotate a player as a staff action.~~ |
| ~~/darkness shuffle <player> <one\|many>~~ | ~~Shuffle a player's inventory slots.~~ |
| ~~/darkness addpoint <player>~~ | ~~Add a punishment point to a player.~~ |
| ~~/darkness resetpoints <player>~~ | ~~Reset a player's punishment points.~~ |
| ~~/darkness punish <player> [reason]~~ | ~~Apply the configured punishment.~~ |
| ~~/darkness ban <player> [reason]~~ | ~~Ban a player using the configured command.~~ |
| ~~/darkness kick <player> [reason]~~ | ~~Kick a player.~~ |
| ~~/darkness crash <player> <method>~~ | ~~Run a configured client stress-test method. Available suggestions: `entity`, `explosion`, `position`, `particle`, `sound`, `loadworld`, `demo`, `fakelag`, `black`, `multi`, `inverse`, `chunk`.~~ |

### ~~Information Hider~~

> [!NOTE]
> This functionality is not available in the free version. Full version: [https://darkness.rip](https://darkness.rip)

~~Information Hider is configured through the InformationHider section in config.yml. It changes
selected data sent to the client, reducing information exposed to cheat
modules. Test options with your gameplay, resource packs, scoreboards, and other plugins.~~

| ~~Settings~~ | ~~What they do~~ |
| --- | --- |
| ~~enabled; onlyForPlayers~~ | ~~Enable the module and limit it to player entities.~~ |
| ~~parts.health.value; random~~ | ~~Send a configured health value from 0 to 20, optionally randomised. Value 0 can disrupt client animations and is not recommended for normal gameplay.~~ |
| ~~parts.xp; air; saturation~~ | ~~Hide experience, air, and food-saturation values.~~ |
| ~~parts.ping; gamemode; seed~~ | ~~Mask tab-list ping, game mode, and world-seed data.~~ |
| ~~parts.scoreboard.enabled; patterns~~ | ~~Mask health values in matching scoreboards and the tab list. Patterns identify health-related objectives; this module is disabled when health value is -1.~~ |
| ~~parts.enchant; item_count; item_name; item_drop; durability~~ | ~~Mask enchantments, quantities, names, dropped-item details, and durability.~~ |
| ~~parts.potion; end_effect~~ | ~~Hide potion effects and send misleading effect packets to reduce the usefulness of cheat effect lists.~~ |
| ~~parts.esp_tracer.enabled; spawn_tab~~ | ~~Create invisible client-side decoy players for ESP tracers and boxes, optionally also in the tab list.~~ |
| ~~parts.inventory~~ | ~~Shuffle items while moving to disrupt AutoTotem and InvWalk-style automation.~~ |
| ~~parts.chest_loot~~ | ~~Briefly hide container contents when opened to disrupt ChestStealer-style automation.~~ |
| ~~parts.totem.enabled; spawn_tab~~ | ~~Enable client-side fake totem-pop decoys and choose whether they appear in tab.~~ |
| ~~parts.totem.mode~~ | ~~Use always for random combat decoys or on_totem to react to a nearby real totem pop.~~ |
| ~~parts.totem.totem_chance; bots; height~~ | ~~Configure chance in always mode, plus decoy count and vertical offset in on_totem mode. High chance values increase network and client load.~~ |
| ~~parts.fake_staff.enabled; only_random_name~~ | ~~Create client-side staff decoys; random-only names can improve compatibility but may be easier for some lists to filter.~~ |
| ~~parts.fake_staff.roles; bots~~ | ~~Configure displayed staff prefixes and the number of staff decoys.~~ |
| ~~parts.spectators.allowed_worlds; hide_spectators~~ | ~~Restrict spectator hiding to listed worlds, and hide players with darkness.spectator permission, including those not currently spectating.~~ |

### Building from source

```bash
git clone https://github.com/1hendex/DarknessFree.git
cd DarknessFree
./gradlew shadowJar
```

On Windows, use `gradlew.bat shadowJar`. The shaded plugin JAR is written to:

```text
build/libs/Darkness.jar
```

### Support and credits

- Official Website: [https://darkness.rip](https://darkness.rip)
- Community and support: [Darkness Liquidation Discord](https://darkness.rip)
- Purchase the full version: [https://darkness.rip](https://darkness.rip)

Darkness is a fork of the open-source [GrimAC](https://github.com/GrimAnticheat/Grim)
project. Credit and thanks go to the GrimAC contributors for the prediction engine and the work that
made this project possible. Darkness-specific code, checks, tools, and configuration are maintained
by the Darkness Liquidation team.
