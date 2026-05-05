package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Stimpack;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;

import java.util.ArrayList;

public class GaussRifle extends MeleeWeapon {

    public static final String AC_ZAP = "ZAP";

    {
        image = ItemSpriteSheet.GaussRifle;
        hitSound = Assets.Sounds.MARINGUN;
        hitSoundPitch = 1f;

        tier = 5;
        RCH = 2;
    }

    @Override
    public int max(int lvl) {
        return 5 * tier + lvl * tier; //25 + 6
    }

    @Override
    public int min(int lvl) {
        return 8+lvl;
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_ZAP);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {

        if (action.equals(AC_ZAP)) {
            int cost = Math.round(hero.HT * 0.25f);

            if (hero.HP <= cost) {
                GLog.w("체력이 부족합니다");
                return;
            }

            Stimpack activeStim = hero.buff(Stimpack.class);
            if (activeStim != null) {
                activeStim.detach();
            }

            Buff.affect(hero, Stimpack.class, 6f);
        }
        else {
            super.execute(hero, action);
        }
    }

    //무기 기본 공격 속도 조작
    @Override
    public float speedFactor(Char owner) {

        float delay = super.speedFactor(owner);

        if (owner != null && owner.buff(Stimpack.class) != null) {

            return delay * 0.35f;
        }

        return delay;
    }
}