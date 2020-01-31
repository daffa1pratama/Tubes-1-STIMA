***** RULES TOWER DEFENCE *****

# GOAL
Membuat strategi terbaik untuk membangun building pada map. Harus memperhatikan defense dan offense.

# PLAYER
Pemain dapat berupa :
- Player vs Player
- Bot vs Player
- Bot vs Bot

# MAP
- Ukuran map **16 x 8** dan dibagi menjadi 2 bagian
- Mirrored
- Pemain melihat peta di sebelah kiri pembatas
- Daerah pemain (0,0) sampai (7,7)
- Format : **['x' 'building' 'y']** | [0A0] = Attack berada di (0,0)
- Animasi missile berupa '>' atau '<'

# ECONOMY
- Start : **20 energy**
- Per turn : **5 energy**

# GAMEPLAY
- Command format : **x,y,type**
- Valid command :
    * 0 : **Build Defence Tower**
    * 1 : **Build Attack Tower**
    * 2 : **Build Energy Building**
    * 3 : **Deconstruct building**
    * 4 : **Build Tesla Tower**
    * 5 : **Activate Iron Curtain**
- Invalid command = end turn:
    * Ketika build, tidak cukup energy
    * Ketika build, bangunan sudah ada
    * Ketika build, membangun di area musuh
- Do Nothing = end turn
- Build/destroy building akan menambah score
- Maximum turn **400** per side

# BUILDING
**DEFENCE BUILDING**
* Cost: **30**
* Health: **20**
* Construction time: **3**
* Constructed character: **D**
* Under construction character: **d**
* Untuk menahan serangan missile musuh

**ATTACK BUILDING**
* Cost:    **30**
* Health: **5**
* Firing rate: **3**
* Missile speed: **2**
* Damage: **5**
* Construction time: **1**
* Constructed character: **A**
* Under construction character: **a**
* Untuk menyerang musuh dengan missile

**ENERGY BUILDING**
* Cost:    **20**
* Health: **5**
* Energy generated per turn: **3**
* Construction time: **1**
* Constructed character: **E**
* Under construction character: **e**
* Untuk menambah energy per turn

**TESLA TOWER**
* Cost:    **300**
* Health: **5**
* Damage: **20** per hit
* Construction time: **10**
* Constructed character: **T**
* Energy needed per attack:  **100**
* Under construction character: **t**
* Maximum Range: **9**
* Maximum amount of towers per person: **2**
* Untuk menyerang dengan multiple attack

**IRON CURTAIN**
* Cost:    **100**
* Active rounds: **6**
* Reset period: **30**
* Untuk menghancurkan semua missile musuh
* Dapat ditembus tesla tower

# MISSILE
- Hancur ketika menabrak bangunan
- Missile akan menyerang base ketika tidak ada bangunan didepannya (HP player --)
- Arah serang lurus

# LIGHTNING ATTACK (TESLA TOWER)
- Instant attack
- Attack 3 rows (threepeater PvZ)

# SCORE
- Didapat dari :
    * Total damage dealt (Building/HP Musuh)
    * Building constructed
    * Total energy generated

