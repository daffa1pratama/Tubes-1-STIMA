*Game Start*

if *enemy build attack*
    *ai build defense*
else if *enemy build defense* or *enemy build energy*
    *ai build energy*
else if *ai energy < 30*
    *ai build energy*
else if *ai energy >= 30* or *ai energy < 60*
    *ai build energy*
else if *ai energy > 60*
    *ai build attack*
else if *ai energy > 300* and *energy gain next turn > 100*
    *ai build tesla*
else if *enemy attack in 7 row* and *ai energy > 100*
    *ai use iron curtain*

# MAPPING
Column : (prioritize lowest column)
    0 - 1   : energy building
    2 - 4   : attack building
    5       : tesla tower
    6 - 7   : defence building

0   1   2   3   4   5   6   7
E   E   A   A   A   T   D   D  

if *ai build energy*
    if *enemy doesnt build attack*
        *build energy at column 0 & random row*
        if *column 0 is full*
            *build at column 1 & random row*
    if *enemy build attack at row i*
        *build energy at column 0 & row 4+i*
        if *column 0 is full*
            *build at column 1 & row 4+i*

else if *ai build attack*
    if (*enemy have defense at row i* or *enemy build defense at row i*) and *enemy defense not full row*
        *build attack at column 2 & row i+1 or i-1*
        if *column 3 is full*
            *build attack at column 3 or 4 & row i+1 or i-1*
    else if *enemy defense full row*
        *build attack behind defense*

else if *ai build defense*
    if *enemy missile comming at row i*
        *build defense at row i*
    else
        *build at row energy*


=================REFERENCE BOT======================
if (enemy punya *attack* && player tidak punya *defense*){
    BUILD DEFENSE
}
if (enemy tidak punya *attack* && player tidak punya *energy*){
    BUILD ENERGY
}
if (player punya *defense*){
    BUILD ATTACK
}
if (player do nothing){
    BUILD ATTACK/DEFENSE
}

BOT         ENEMY
energy      energy
attack      defense
energy      attack
defense     defense