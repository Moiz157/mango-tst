# MangoParty

MangoParty is a comprehensive Minecraft server plugin for Spigot/Paper 1.21.4 that provides a full-featured party and PvP combat system. It allows players to form parties, queue for matches, challenge others to duels, fight in custom arenas, and compete with custom kits.

## Features

### 1. Party System
- **Create & Invite:** Players can create parties and invite up to 8 members.
- **Manage:** Leaders can kick members, transfer leadership, or disband the party.
- **Chat:** Dedicated party chat channel (`!message` or `@message`).
- **Persistence:** Party state is managed in memory for the session.

### 2. Match Types
- **Party Split Match:** Party is divided into two balanced teams. Last team standing wins.
- **Party FFA:** Free-for-all within the party. Last player alive wins.
- **Party vs Party Duel:** Challenge another party leader. Party 1 vs Party 2.
- **1v1 Duel:** Challenge any player to a 1v1 duel.
- **Queue System:** Auto-matchmaking for 1v1, 2v2, and 3v3 matches with specific kits.

### 3. Arena System
- **Management:** Admins can create, edit, and delete arenas via commands or GUI.
- **Auto-Regeneration:** Arenas are automatically regenerated after matches using WorldEdit schematics.
- **Instancing:** The system creates dynamic instances of arenas to support concurrent matches.

### 4. Kit System
- **Custom Kits:** Create kits from your inventory.
- **Rules:** Configure rules per kit (Health Regen, Block Breaking, Block Placing, Damage Multiplier, Instant TNT).
- **GUI Editor:** Easily manage kit settings via an inventory GUI.

### 5. Scoreboard
- **Real-time Stats:** FastBoard-powered sidebar showing match info, kills, alive players, and time.
- **Customizable:** Fully configurable title and lines via `scoreboard.yml`.
- **Hex Colors:** Full support for `&#RRGGBB` color codes.

### 6. GUIs
- **User-Friendly:** Inventory-based menus for Match Types, Kit Selection, Arena Editing, and Kit Rules.

## Installation

1. **Prerequisites:**
   - Java 17 or higher.
   - Spigot or Paper 1.21.4 server.
   - **WorldEdit 7.3.0+** and **FastAsyncWorldEdit (FAWE) 2.9.2+** installed on the server.

2. **Install:**
   - Drop `mangoparty-1.0.0.jar` into your server's `plugins/` folder.
   - Restart the server.

3. **Configuration:**
   - Config files are generated in `plugins/MangoParty/`.
   - `config.yml`: General settings (timeouts, limits).
   - `scoreboard.yml`: Scoreboard layout.
   - `arenas.yml` & `kits.yml`: Data storage (do not edit manually while server is running).

## Setup Guide

### Creating an Arena
1. Build your arena in the world.
2. Select the region using WorldEdit wand (`//wand`).
3. Copy the region (`//copy`).
4. Create the arena in MangoParty:
   `/mango arena create <name>`
5. Set key locations (stand at location and run command):
   - `/mango arena corner1 <name>` (automatically uses WE selection if available/logic updated)
   - `/mango arena corner2 <name>`
   - `/mango arena center <name>` (center point for pasting)
   - `/mango arena spawn1 <name>` (Team 1 spawn)
   - `/mango arena spawn2 <name>` (Team 2 spawn)
6. Save the schematic:
   `/mango arena save <name>`
   *(This saves the current WorldEdit selection as the schematic for regeneration)*

### Creating a Kit
1. Arrange your inventory (items, armor) exactly how you want the kit.
2. Run: `/mango create kit <name>`
3. Edit rules (optional): `/mango kit editor`

## Commands

### Player Commands
- `/party create` - Create a new party.
- `/party invite <player>` - Invite a player.
- `/party join <leader>` - Join a party.
- `/party leave` - Leave current party.
- `/party match` - Open match type GUI (Leader only).
- `/party challenge <leader>` - Challenge another party.
- `/duel <player>` - Challenge a player to 1v1.
- `/spectate <player>` - Spectate a teammate in a match.
- `/1v1queue`, `/2v2queue`, `/3v3queue` - Join matchmaking queues.
- `/leavequeue` - Leave the queue.

### Admin Commands
- `/mango arena ...` - Arena management.
- `/mango kit ...` - Kit management.
- `/mango setspawn` - Set global spawn (fallback).

## Permissions
- `mangoparty.admin` - Access to `/mango` commands (default: op).
- `mangoparty.party` - Access to party commands (default: true).
- `mangoparty.duel` - Access to duel commands (default: true).
- `mangoparty.queue` - Access to queue commands (default: true).
- `mangoparty.spectate` - Access to spectate command (default: true).
