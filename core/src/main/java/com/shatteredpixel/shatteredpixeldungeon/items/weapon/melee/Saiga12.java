package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Saiga12 extends ShotgunWeapon {

    {
        image = ItemSpriteSheet.SAIGA12;
        hitSound = Assets.Sounds.SAIGA12;
        hitSoundPitch = 0.9f;

        FIRE_DELAY_MULT = 0.5f;
        bulletMax = 11;
        bullet = bulletMax;
        MAX_RANGE = 5;

        PELLET_COUNT = 5;
        CONE_DEGREES = 27f;
        EXTRA_PELLET_MULT = 0.33f;

        usesTargeting = true;
        defaultAction = AC_ZAP;
        tier = 5;
    }

    @Override
    public int fireMin() {
        return bulletTier + level()
                + RingOfSharpshooting.levelDamageBonus(Dungeon.hero);
    }

    @Override
    public int fireMax() {
        return 5 + bulletTier * 3 + level() * 5
                + RingOfSharpshooting.levelDamageBonus(Dungeon.hero) * 2;
    }

    @Override
    protected void specialFire(Char ch) {

        if (ch.isAlive()) {
            Buff.affect(ch, Bleeding.class).set(7);
            Buff.affect(ch, Cripple.class, 1f);
        }

        if (ch.sprite != null) {
            ch.sprite.emitter().burst(BloodParticle.FACTORY, 5);
        }
    }
}