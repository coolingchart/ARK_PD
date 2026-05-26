package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Random;

public class Zealotknife extends MeleeWeapon {

    {
        image = ItemSpriteSheet.Zealotknife;
        hitSound = Assets.Sounds.ZEALOTHIT;
        hitSoundPitch = 1f;
        tier = 5;
    }

    @Override
    public int max(int lvl) {
        return (2 * tier + 4) + (lvl * 3);
    }

    @Override
    public int min(int lvl) {
        return tier + lvl; // 기본 최소 데미지
    }

    @Override
    public int damageRoll(Char owner) {

        if (owner == null) return super.damageRoll(owner);

        float hpPercent = (float) owner.HP / owner.HT;

        int baseMax = max(level());
        int baseMin = min(level());

        float multiplier;

        if (hpPercent >= 0.90f) {
            multiplier = 1.2f;
        } else if (hpPercent >= 0.80f) {
            multiplier = 1.1f;
        } else if (hpPercent >= 0.70f) {
            multiplier = 1.0f;
        } else if (hpPercent >= 0.60f) {
            multiplier = 0.9f;
        } else if (hpPercent >= 0.50f) {
            multiplier = 0.8f;
        } else {
            multiplier = 0.7f;
        }

        int finalMax = Math.round(baseMax * multiplier);
        int finalMin;

        if (multiplier >= 1.1f) {
            finalMin = Math.round(finalMax * 0.8f);
        }
        else {
            finalMin = Math.min(baseMin, finalMax);
        }

        return Random.NormalIntRange(finalMin, finalMax);
    }

    @Override
    public String desc() {
        String originalDesc = super.desc();

        if (Dungeon.hero != null && Dungeon.hero.isAlive()) {

            float hpPercent = (float) Dungeon.hero.HP / Dungeon.hero.HT;
            int baseMax = max(level());
            int baseMin = min(level());
            float multiplier;

            if (hpPercent >= 0.90f) {
                multiplier = 1.2f;
            } else if (hpPercent >= 0.80f) {
                multiplier = 1.1f;
            } else if (hpPercent >= 0.70f) {
                multiplier = 1.0f;
            } else if (hpPercent >= 0.60f) {
                multiplier = 0.9f;
            } else if (hpPercent >= 0.50f) {
                multiplier = 0.8f;
            } else {
                multiplier = 0.7f;
            }

            int finalMax = Math.round(baseMax * multiplier);
            int finalMin;

            if (multiplier >= 1.1f) {
                finalMin = Math.round(finalMax * 0.8f);
            } else {
                finalMin = Math.min(baseMin, finalMax);
            }
            int percent = Math.round(multiplier * 100);
            return originalDesc + "\n\n현재 데미지 배율: +" + percent + "%\n현재 데미지: " + finalMin + " ~ " + finalMax;
        }
        return originalDesc;
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {

        if (defender.isAlive()) {
            defender.damage(attacker.damageRoll() - defender.drRoll(), attacker);
            defender.sprite.burst(CharSprite.NEGATIVE, 10);
        }

        return super.proc(attacker, defender, damage);
    }

}