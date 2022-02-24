# OlympaZTA

ZTA was a game made for Olympa.

## Gameplay
It features a "post-apocalyptic" world where humans turn into zombies when they die, due to an an epidemic or biotech disaster.

The world is thus full of zombies of multiple types (common, ninja, explosive...)
which spawn randomly thanks to a custom algorithm.

The map is split into zones with different loots, different zombie types distribution.

The main gameplay is made of looting chests in the world which give stuff like guns, armor, food, ammos...
It is a mainly "grinding" game, where the goal is always to find better stuff and earn money.

## Side objectives
* Many quests on the world, using an external quests plugin
* "Bounty hunting" system
* Plots on a separate, safe world, for players to build a little house/storage room/farming zone (they can grow food and fish). They first buy a 10x10 plot and then they pay to expand it until they reach 48x48 blocks. Building blocks can be bought at a multiple shops.

## Clans system
Players can arrange themselves into clans, which are basically group of players.
Clans have a basic hierarchy of chief > players.

A clan can own a plot on the world, which are specific house/flat defined by the staff and rent it a set price per week.

## Various features
* zipline :)
* glass can be broken and will auto-regenerate after a set time
* enderchest system
* money is physical (banknotes) and must be turned into virtual money on your bank account at a bank office
* shops to sell food/junk items or buy guns/building blocks
* configurable per-player features: display of members in scoreboard, display of quests in scoreboard, mobs health bars, region title, blood effects, and ambient sounds

## Dependencies:
* BeautyQuests: https://github.com/SkytAsul/BeautyQuests
* Citizens: https://github.com/CitizensDev/Citizens2/
* Sentinel: https://github.com/mcmonkeyprojects/Sentinel
* Dynmap: https://github.com/webbukkit/dynmap

## Codebase:
* Code is in english, but most strings are in french
* Gradle
* Java 16
* Spigot 16
* OlympaAPI