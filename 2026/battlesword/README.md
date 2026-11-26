# Battlesword

A tool to help manage a Warhammer Fantasy Roleplay 2nd Edition campaign.

Not much here yet, so, roadmap:

- [ ] Basic website stuff.
  - [ ] Some sort of basic frontend (React/TS, Replicant/cljs, ...).
  - [ ] Web hosting, with some form of backend code.
  - [ ] Database / persistence layer.
  - [ ] Login / auth support.
- [ ] Purchase helper (stateless): we have our own rules for item availability & prices, and they are a little cumbersome, so some automation would help a lot.
  - [ ] Encode the rules.
  - [ ] Import item database.
  - [ ] Make some UI.
  - [ ] Support both automation & manual rolls.
  - This is the first useful milestone.
- [ ] Inventory management: first per-login data, also, persistent data.
  - [ ] Need a concept of campaign, with a GM & some players.
  - [ ] Players have characters.
  - [ ] Characters have an inventory.
  - [ ] Invetory changes over time: loot, rewards, purchases.
  - [ ] Full history should be visible.
  - [ ] GM rules: do they need to validate things? Can they arbitrarily change things?
- [ ] Career tracking.
  - [ ] Import careers data.
  - [ ] Visualize career paths, from a given career.
  - [ ] Allow characters to have a career.
  - [ ] Track XP & advancements.
  - [ ] Visualize career paths, from a character. Count & display required XP.
  - [ ] Allow changing careers.

I think that's a good enough list to get started.
