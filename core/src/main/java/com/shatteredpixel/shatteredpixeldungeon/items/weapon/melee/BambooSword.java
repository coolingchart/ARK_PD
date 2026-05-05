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

    // 1. 무기를 장착할 때 상태 감시 타이머를 영웅에게 부여합니다.
    @Override
    public boolean doEquip(Hero hero) {
        super.doEquip(hero);
        if (hero.buff(AcrophobiaTimer.class) == null) {
            Buff.affect(hero, AcrophobiaTimer.class);
        }
        return false;
    }

    // 2. 무기를 해제할 때 타이머와 약화 효과를 모두 깔끔하게 지웁니다.
    @Override
    public boolean doUnequip(Hero hero, boolean rm) {
        super.doUnequip(hero, rm);
        Buff.detach(hero, AcrophobiaTimer.class);
        Buff.detach(hero, Weakness.class); // 해제 시 남은 약화도 지워줌
        return rm;
    }

    // 3. 매 턴 부유 상태를 감시하는 전용 타이머 버프 클래스입니다.
    public static class AcrophobiaTimer extends Buff {
        @Override
        public boolean act() {
            if (target instanceof Hero) {
                Hero hero = (Hero)target;

                // 부유 시 약화 100턴 고정 유지 (고소공포증 패널티)
                if (hero.buff(Levitation.class) != null) {
                    if (hero.buff(Weakness.class) == null) {
                        // 처음 부유를 시작할 때 100턴을 부여합니다.
                        Buff.affect(hero, Weakness.class, 100f);
                    } else {
                        // 이미 약화가 있다면, 매 턴 소모되는 1턴(1f)만큼만 보충하여 100턴을 유지합니다.
                        Buff.affect(hero, Weakness.class, 1f);
                    }
                } else {
                    // 부유 상태가 풀려 땅에 닿으면 약화를 즉시 해제합니다.
                    Buff.detach(hero, Weakness.class);
                }
            } else {
                detach();
            }
            spend(TICK); // 1턴 주기로 계속 감시합니다.
            return true;
        }
    }

    @Override
    public String desc() {
        return "_\"한 번에 간다, 하야테군!\"_\n\n카츠라 히나기쿠가 사용하는 죽도로 낮은 확률로 _치명타_를 날릴 수 있지만 _부유 시 고소공포증으로 인한 약화 효과를 얻습니다._\n\n_무언가와 조합 할 수 있는 신비한 힘이 깃들여져 있는것 같습니다_";
    }
}