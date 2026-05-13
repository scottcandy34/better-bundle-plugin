# BetterBundle Plugin for PaperMC 26.1.2

Recreated from 
the `bundles_mod` branch of the original Fabric mod as a 
modern **PaperMC plugin** using the latest Gradle Kotlin 
DSL and Paper 26.1.2 best practices (paperweight-userdev 
+ Mojang mappings).

## Features (matching & enhancing th
e original mod)
- **Custom Bundle item** using vanilla `B
UNDLE` material for familiarity.
- **Crafting recipe**: S
haped recipe using **5 Rabbit Hide** and **2 String**.
- 
**Weight-based capacity**: Max weight **64**. Most items 
= 1 weight. Tools, armor, weapons, Ender Pearls, etc. = 4
 weight.
- **Blocked items**: Shulker boxes, chests, barr
els, other bundles cannot be stored.
- **Intuitive usage*
* (no GUI needed):
  - **Right-click** while holding Bund
le in main hand + item in **off-hand** â†’ Adds item(s) t
o bundle (respects weight).
  - **Right-click** with empt
y off-hand â†’ Removes the **last item** (LIFO) and gives
 it to you.
  - **Sneak + Right-click** â†’ Empties the e
ntire bundle (drops all items).
- Dynamic lore showing cu
rrent weight / capacity.
- Built with modern **Adventure 
API** for text.
- Fully compatible with **Paper 26.1.2** 
(Mojang mappings, no remapping needed).

## Project Struc
ture (Modern 26.1.2 Setup)
```
better-bundle-plugin/
â”œâ
”€â”€ build.gradle.kts          # Latest paperweight-user
dev 2.0.0-beta.21 + Kotlin DSL
â”œâ”€â”€ settings.gradle.
kts
â”œâ”€â”€ src/
â”‚   â”œâ”€â”€ main/
â”‚   â”‚   â”œâ
”€â”€ java/com/scottcandy34/betterbundle/BetterBundlePlug
in.java
â”‚   â”‚   â””â”€â”€ resources/
â”‚   â”‚       
â””â”€â”€ plugin.yml
â””â”€â”€ README.md
```

## How to B
uild & Run
1. Open the project in **IntelliJ IDEA** (reco
mmended) or any IDE with Gradle support.
2. Make sure you
 have **Java 21+** JDK.
3. Sync Gradle.
4. Run `./gradlew
 build` (or use the Gradle wrapper / IDE task).
5. The pl
ugin JAR will be in `build/libs/better-bundle-plugin-1.0.
0-SNAPSHOT.jar`
6. Drop it into your **Paper 26.1.2** ser
ver's `plugins/` folder.
7. Restart the server.

**Note**
: This project uses the modern `paperweight.userdev` with
 `MOJANG_PRODUCTION` mappings for best compatibility on P
aper 26.1.2+.

## Usage In-Game
- Craft the Bundle (or us
e `/give @s bundle` and it will work as our custom one if
 it has our data).
- Hold the Bundle in your **main hand*
*.
- Put items in your **off-hand** and right-click to st
ore them.
- Right-click again to retrieve items one by on
e (LIFO).
- Sneak + right-click to dump everything.

## C
onfiguration / Future Improvements
- Currently hardcoded 
weights and blocked items (easy to move to config.yml).
-
 Can be extended with commands, permissions, or a GUI bac
kpack mode.
- Tags support (`bundle_ignored_*`) can be ad
ded via config or Minecraft tags.

This plugin brings the
 spirit of the original `bundles_mod` (early bundle for o
lder versions) into the modern Paper era with clean, main
tainable code.

Created following PaperMC 26.1.2 best pra
ctices and your requested style. 

Enjoy your bundles! ðŸ
Ž’
