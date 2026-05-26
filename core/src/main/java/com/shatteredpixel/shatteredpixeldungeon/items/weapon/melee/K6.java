package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class K6 extends GunWeapon {

    {
        image = ItemSpriteSheet.K6;

        hitSound      = Assets.Sounds.M2MG;
        hitSoundPitch = 1.0f;

        FIRE_DELAY_MULT = 0.9f;
        bulletMax       = 101;
        bullet          = Random.Int(bulletMax / 2, bulletMax + 1);

        MIN_RANGE = 3;
        MAX_RANGE = 10;

        usesTargeting = true;
        defaultAction = AC_ZAP;

        tier = 5;
    }

    @Override
    public int STRReq(int lvl) {
        return STRReq(tier + 2, lvl);
    }

    @Override
    public int fireMax() {
        return (int) 4
                + tier * 10
                + bulletTier * 3
                + level() * (tier + 1)
                + RingOfSharpshooting.levelDamageBonus(Dungeon.hero) * 2;
    }
    @Override
    public float getFireAcc(int from, int to) {
        int distance = getDistance(from, to);

        if (isWithinRange(distance)) {
            return 1.5f;
        } else if (distance > getMaxRange()) {
            return Math.max(0f, 1f - 0.2f * (distance - getMaxRange()));
        } else if (distance < getMinRange()) {
            return 0.6f;
        }
        return 1f;
    }

    @Override
    protected void specialFire(Char ch) {
        if (!ch.properties().contains(Char.Property.BOSS)
                && !ch.properties().contains(Char.Property.MINIBOSS)) {
            Buff.affect(ch, Terror.class, Terror.DURATION);
        }
    }



    @Override
    protected void onZap(Ballistica bolt) {
        float oldacc = ACC;
        try {
            Char ch = Actor.findChar(bolt.collisionPos);
            if (ch != null) {
                Buff.affect(Dungeon.hero, RangedAttackTracker.class);
                processGunHit(ch, 1f, true);
            } else {
                Dungeon.level.pressCell(bolt.collisionPos);
            }
            postShotCleanup(null, false, false);
        } finally {
            ACC = oldacc;
        }
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", bulletTier);
    }
}