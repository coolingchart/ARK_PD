package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Sig553 extends GunWeapon {
    {
        image = ItemSpriteSheet.SIG553;
        hitSound = Assets.Sounds.HIT_GUN;
        hitSoundPitch = 0.9f;

        FIRE_DELAY_MULT = 0.5f;
        bulletMax = 31;
        MIN_RANGE = 2;
        MAX_RANGE = 6;

        usesTargeting = true;

        defaultAction = AC_ZAP;

        tier = 3;
    }

    @Override
    public int fireMax() {
        return (int) 3
                + tier * 3
                + bulletTier * 2
                + level() * 2
                + RingOfSharpshooting.levelDamageBonus(Dungeon.hero) * 2;
    }

    @Override
    public float getFireAcc(int from, int to) {
        int distance = getDistance(from, to);

        // 최대 사거리 10, 최소사거리 2, 유효 사거리 2-6, 2배 보정
        if (distance > getMaxRange()) {
            return Math.max(0f, 1f - 0.25f * (distance - getMaxRange()));
        } else if (distance < getMinRange()) {
            return 0.67f;
        } else {
            return 2f;
        }
    }

    @Override
    protected void specialFire(Char ch) {
        Buff.affect(ch, Silence.class, 3f);
    }
}
