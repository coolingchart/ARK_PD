package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CloserangeShot;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.watabou.utils.Random;

public class AKM extends GunWeapon {
    {
        image = ItemSpriteSheet.AKM;
        hitSound = Assets.Sounds.AKMHIT;
        hitSoundPitch = 1f;

        FIRE_DELAY_MULT = 0.85f;
        bulletMax = 31;
        bullet = Random.Int(bulletMax / 2, bulletMax + 1);
        MIN_RANGE = 2;
        MAX_RANGE = 6;

        usesTargeting = true;
        defaultAction = AC_ZAP;
        tier = 5;
    }

    @Override
    public int fireMax() {
        return (int) 4
                + tier * 6
                + bulletTier * 3
                + level() * (tier + 1)
                + RingOfSharpshooting.levelDamageBonus(Dungeon.hero) * 2;
    }

    @Override
    public float getFireAcc(int from, int to) {
        int distance = getDistance(from, to);

        if (distance >= 2 && distance <= 4) {
            return 1.3f;
        } else if (distance == 5 || distance == 6) {
            return 1.0f;
        } else if (distance >= 7) {
            return Math.max(0f, 1f - 0.2f * (distance - 6));
        } else if (distance < 2) {
            return 0.67f;
        }

        return 1f;
    }

    // 특수 사격 시 출혈과 불구(1턴) 동시 부여
    @Override
    protected void specialFire(Char ch) {

        if (ch.isAlive()) {
            Buff.affect(ch, Bleeding.class).set(10);
            Buff.affect(ch, Cripple.class, 1f);
        }

        if (ch.sprite != null) {
            ch.sprite.emitter().burst(BloodParticle.FACTORY, 5);
        }
    }

    @Override
    protected void onZap(Ballistica bolt) {
        CloserangeShot closerRange = Dungeon.hero.buff(CloserangeShot.class);
        float oldacc = ACC;
        boolean anyKill = false;
        try {
            Char ch = Actor.findChar(bolt.collisionPos);
            if (ch != null) {
                Buff.affect(Dungeon.hero, RangedAttackTracker.class);

                int oldHp = ch.HP;

                processGunHit(ch, 1f, true);

                //확률조정
                if (ch.HP < oldHp && Random.Int(100) < 10) {
                    specialFire(ch);
                }

                if (!ch.isAlive()) anyKill = true;
            } else {
                Dungeon.level.pressCell(bolt.collisionPos);
            }
            postShotCleanup(closerRange, false, anyKill);
        } finally {
            ACC = oldacc;
        }
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", bulletTier);
    }
}