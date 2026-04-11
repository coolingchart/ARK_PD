package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Camouflage;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CivilianSprite;

public class Dummy extends Mob {
    {
        spriteClass = CivilianSprite.class;
        HP = HT = 1000;
        //properties.add(Property.IMMOVABLE);

        state = PASSIVE;
        immunities.add(Corruption.class);
    }

    @Override
    public void beckon(int cell) {
        //do nothing
    }

    @Override
    protected boolean act() {
        if (Camouflage.CamoFlageEnemy(this)) Buff.affect(this, Camouflage.class, 10f);
        if (buff(Corruption.class) != null) {
            buff(Corruption.class).detach();
        }

        HP = Math.min(HP + 50, HT);
        return super.act();
    }

    public static void spawn(Level level, int poss) {
        Dummy WhatYourName = new Dummy();
        do {
            WhatYourName.pos = poss;
        } while (WhatYourName.pos == -1);
        level.mobs.add(WhatYourName);
    }

    {
        immunities.add(Amok.class);
        immunities.add(Sleep.class);
        immunities.add(Terror.class);
        immunities.add(Vertigo.class);
    }
}
