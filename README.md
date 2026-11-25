# TokenShop

A lightweight **token economy** with a fully configurable **GUI shop** for Paper servers.
Give players tokens for voting, events or playtime, and let them spend tokens on items, ranks or anything you can run as a command.

![Minecraft](https://img.shields.io/badge/Minecraft-1.21-green) ![Paper](https://img.shields.io/badge/Server-Paper-blue) ![Java](https://img.shields.io/badge/Java-21+-orange)

## Features
- Token balance for every player, saved to `tokens.yml` with autosave
- Pay other players, plus admin give / take / set
- GUI shop built entirely from `config.yml`: slots, items, names, lore, prices
- Each shop item runs any console commands on purchase (give items, add LuckPerms ranks, etc.)
- Every message is customizable with `&` color codes
- Tab completion, sounds on purchase and on failed purchase
- Item stealing out of the GUI is blocked
- `/tokenshop reload` without restarting the server

## Commands
| Command | Description | Permission |
|---|---|---|
| `/tokens` | Check your balance | `tokenshop.use` |
| `/tokens <player>` | Check another player's balance | `tokenshop.use` |
| `/tokens pay <player> <amount>` | Send tokens to a player | `tokenshop.use` |
| `/tokens give <player> <amount>` | Give tokens | `tokenshop.admin` |
| `/tokens take <player> <amount>` | Take tokens | `tokenshop.admin` |
| `/tokens set <player> <amount>` | Set a balance | `tokenshop.admin` |
| `/tokenshop` | Open the shop | `tokenshop.use` |
| `/tokenshop reload` | Reload the config | `tokenshop.admin` |

`tokenshop.use` is given to everyone by default. `tokenshop.admin` is given to OPs by default.

## Adding a shop item
```yaml
shop:
  items:
    my-item:
      slot: 13
      material: DIAMOND_SWORD
      amount: 1
      name: "&cEpic Sword"
      lore:
        - "&7Price: &e%price% tokens"
      price: 100
      commands:
        - "give %player% diamond_sword 1"
```

## Installation
1. Download `TokenShop-1.0.0.jar` from Releases.
2. Put it in your server's `plugins` folder.
3. Restart the server and edit `plugins/TokenShop/config.yml`.

## Building from source
```bash
./gradlew build
```
The jar appears in `build/libs/`. Builds with Java 21 or newer. The plugin runs on Java 21+ Paper 1.21 servers.

## Ideas for next versions
- PlaceholderAPI support (`%tokenshop_balance%`)
- MySQL storage for networks
- Multiple shop pages and categories
- Purchase limits and cooldowns

---
Need a custom plugin like this for your server? Contact me.
