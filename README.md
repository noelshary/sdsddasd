# Nova Client

Alles-in-einem-Client als Fabric-Mod (Minecraft 1.21.11, Java 21). Menü mit **Right Shift**.

## Bauen über GitHub
1. Neues GitHub-Repo anlegen, den Inhalt dieses Ordners hochladen (die ZIP entpacken, dann alles committen).
2. Reiter **Actions** → Workflow `build` läuft automatisch bei jedem Push.
3. Fertige Datei: im Run unter **Artifacts** → `novaclient` → die `.jar` (nicht die `-sources.jar`) in den `mods`-Ordner.
   Du brauchst Fabric Loader + Fabric API.

### Wenn der Build bei den Versionen scheitert
In `gradle.properties` stehen Versionen, die ich nicht prüfen konnte. Einfach diese Zeilen aus deinem
funktionierenden 1.21.11-Projekt übernehmen: `minecraft_version`, `yarn_mappings`, `loader_version`,
`loom_version`, `fabric_version`. Nutzt dein Projekt Mojang-Mappings statt Yarn, sag Bescheid.
Compile-Fehler im Code: die Meldung aus Actions kopieren und mir schicken, dann fixe ich es.

## Bedienung
- **Right Shift**: Menü auf/zu
- **Linksklick** auf ein Modul: an/aus, **Rechtsklick**: Einstellungen aufklappen, Slider mit gedrückter Maus ziehen

## Module
**Bewegung:** Fly (optional durch Blöcke), Elytra Fly
**Kampf:** Stun Slam, Elytra Stun Slam, Fast Crystal, Fast Anchor, Auto Totem
**Render:** Spieler ESP, Block ESP (Spawner mit Mob-Name + Farbe, Truhen, Endertruhen, Shulker), X-Ray, Fullbright

### Hinweise
- Stun Slam: Mace in die Hand, auf den Gegner springen und zuschlagen. Die Mod haut davor automatisch mit Axt
  (Gegner blockt) oder Schwert zu, ohne dass du switchst. Delay 0 = gleicher Tick, 1-4 = später.
  Der Schaden hängt von der echten Fallhöhe ab, die Mod fälscht keine Fallhöhe.
- Fast Crystal: setzt nur bei Zielen in der Luft, die höher stehen als du (Einstellung "Min. Ziel-Höhe über mir").
  Hört sofort auf, wenn das Ziel tot ist.
- Fast Anchor: Anchor setzen, laden, zünden (Delay 0 = alles im selben Tick). Die Explosion kann dich selbst treffen.
- ESP ist 2D über das HUD und funktioniert nur in der Ego-Perspektive.
- X-Ray läuft nicht mit Sodium (nur mit dem normalen Minecraft-Renderer).
- Auf Servern mit Anticheat führen Fly, NoClip, X-Ray und Zero-Tick-Aktionen schnell zum Bann.
- Einstellungen werden noch nicht gespeichert.
