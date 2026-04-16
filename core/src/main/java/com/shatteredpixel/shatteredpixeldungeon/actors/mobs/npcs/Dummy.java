package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Camouflage;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
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

        state = PASSIVE;
        immunities.add(Corruption.class);
        immunities.add(Doom.class);
        immunities.add(Charm.class);
    }

    @Override
    public void beckon(int cell) {
        //do nothing
    }

    @Override
    protected boolean act() {
        //fix corrupted saves: reset alignment and state if changed by external effects
        alignment = Alignment.ENEMY;
        state = PASSIVE;

        if (Camouflage.CamoFlageEnemy(this)) Buff.affect(this, Camouflage.class, 10f);
        if (buff(Corruption.class) != null) {
            buff(Corruption.class).detach();
        }

        HP = Math.min(HP + 50, HT);
        return super.act();
    }

    public static void spawn(Level level, int poss) {
        Dummy dummy = new Dummy();
        dummy.pos = poss;
        level.mobs.add(dummy);
    }

    {
        immunities.add(Amok.class);
        immunities.add(Sleep.class);
        immunities.add(Terror.class);
        immunities.add(Vertigo.class);
    }
}
