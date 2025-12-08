package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.IsekaiItem;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class R4C extends GunWeapon {
    {
        image = ItemSpriteSheet.R4C;
        hitSound = Assets.Sounds.HIT_AR;
        hitSoundPitch = 0.9f;

        FIRE_DELAY_MULT = 0.5f;
        bulletMax = 31;
        MIN_RANGE = 2;
        MAX_RANGE = 6;

        usesTargeting = true;

        defaultAction = AC_ZAP;

        tier = 5;
    }

    @Override
    public int fireMax() {
        return (int) 4
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
        Ballistica trajectory = new Ballistica(curUser.pos, ch.pos, Ballistica.STOP_TARGET);
        trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);
        WandOfBlastWave.throwChar(ch, trajectory, 2); // 넉백 효과
    }

    @Override
    public String desc() {
       String info = Messages.get(this, "desc", bulletTier);
            if (Dungeon.hero.belongings.getItem(IsekaiItem.class) != null) {
                if (Dungeon.hero.belongings.getItem(IsekaiItem.class).isEquipped(Dungeon.hero))
                    info += "\n\n" + Messages.get( R4C.class, "setbouns");}


        return info;
    }
}
