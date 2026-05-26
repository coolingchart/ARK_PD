package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.watabou.utils.Random;

public class BambooSword extends MeleeWeapon {

    {
        image = ItemSpriteSheet.BAMBOOSWORD;
        hitSound = Assets.Sounds.BAMBOOSWORDHIT;
        hitSoundPitch = 1.0f;

        tier = 2;

        bones = false;
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {

        if (!defender.properties().contains(Char.Property.BOSS) &&
                !defender.properties().contains(Char.Property.MINIBOSS)) {

            //치명타
            if (Random.Int(100) < 10) {

                defender.damage(100, attacker);

                defender.sprite.showStatus(0xFF0000, "치명타");
            }
        }
        return super.proc(attacker, defender, damage);
    }

    @Override
    public boolean doEquip(Hero hero) {
        super.doEquip(hero);
        if (hero.buff(AcrophobiaTimer.class) == null) {
            Buff.affect(hero, AcrophobiaTimer.class);
        }
        return false;
    }

    @Override
    public boolean doUnequip(Hero hero, boolean rm) {
        super.doUnequip(hero, rm);
        Buff.detach(hero, AcrophobiaTimer.class);
        Buff.detach(hero, Weakness.class);
        return rm;
    }

    public static class AcrophobiaTimer extends Buff {
        @Override
        public boolean act() {
            if (target instanceof Hero) {
                Hero hero = (Hero)target;

                if (hero.buff(Levitation.class) != null) {
                    if (hero.buff(Weakness.class) == null) {
                        Buff.affect(hero, Weakness.class, 100f);
                    } else {
                        Buff.affect(hero, Weakness.class, 1f);
                    }
                } else {
                    Buff.detach(hero, Weakness.class);
                }
            } else {
                detach();
            }
            spend(TICK);
            return true;
        }
    }

    @Override
    public String desc() {
        return "_\"한 번에 간다, 하야테군!\"_\n\n카츠라 히나기쿠가 사용하는 죽도로 낮은 확률로 _치명타_를 날릴 수 있지만 _부유 시 고소공포증으로 인한 약화 효과를 얻습니다._\n\n_무언가와 조합 할 수 있는 신비한 힘이 깃들여져 있는것 같습니다_";
    }
}