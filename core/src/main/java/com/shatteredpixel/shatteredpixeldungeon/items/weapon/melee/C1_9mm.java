package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class C1_9mm extends GunWeapon {
    {
        image = ItemSpriteSheet.C1;
        hitSound = Assets.Sounds.HIT_GUN;
        hitSoundPitch = 0.9f;

        FIRE_DELAY_MULT = 0.66f;
        bulletMax = 34;
        MIN_RANGE = 2;
        MAX_RANGE = 5;

        usesTargeting = true;

        defaultAction = AC_ZAP;

        tier = 3;
    }

    @Override
    public float getFireAcc(int from, int to) {
        int distance = getDistance(from, to);

        // 최대 사거리 8, 최소사거리 2, 유효 사거리 2-5, 2배 보정
        if (distance > getMaxRange()) {
            return Math.max(0f, 1f - 0.33f * (distance - getMaxRange()));
        } else if (distance < getMinRange()) {
            return 0.67f;
        } else {
            return 1f;
        }
    }

    @Override
    protected void specialFire(Char ch) {
        Buff.affect(ch, Slow.class, 3f);
    }
}
