package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Usg extends GunWeapon {
    {
        image = ItemSpriteSheet.USG;
        hitSound = Assets.Sounds.HIT_PISTOL;
        hitSoundPitch = 0.9f;

        FIRE_DELAY_MULT = 1f;
        bulletMax = 21;
        MIN_RANGE = 1;
        MAX_RANGE = 3;

        usesTargeting = true;

        defaultAction = AC_ZAP;

        tier = 2;
    }

    @Override
    public float getFireAcc(int from, int to) {
        int distance = getDistance(from, to);

        // 최대 사거리 6, 유효 사거리 3, 근거리시 2배 보정
        if (distance > getMaxRange()) {
            return Math.max(0f, 1f - 0.33f * (distance - getMaxRange()));
        } if (distance > 1) {
            return 1.5f;
        } else {
            return 2f;
        }
    }

    @Override
    protected void specialFire(Char ch) {
        Buff.affect(ch, Slow.class, 3f);
    }
}
