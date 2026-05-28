package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class BlackCamellia extends Artifact {
    private static final int AWAKEN_TURN    = 1600;   // 각성 턴 1600
    private static final int LEVEL_UP_TURN  = 80;   // 강화 1회당 턴
    private static final int MAX_LEVEL      = 10;  // 최대 강화 수치

    private static final int MAX_TURN       = AWAKEN_TURN + LEVEL_UP_TURN * MAX_LEVEL;

    public float triesToDrop = Float.MIN_VALUE;
    public int   dropsToRare = Integer.MIN_VALUE;
    public int     turnCount  = 0;
    public boolean isAwakened = false;

    private static final String TURN_COUNT_KEY    = "turnCount";
    private static final String IS_AWAKENED_KEY   = "isAwakened";
    private static final String TRIES_TO_DROP_KEY = "triesToDrop";
    private static final String DROPS_TO_RARE_KEY = "dropsToRare";
    private static final String AC_ZAP = "ZAP";

    {
        image = ItemSpriteSheet.ARTIFACT_BlackCamellia;
    }

    public int camelliaLevel() {
        if (!isAwakened) return 0;
        int awakenedTurns = turnCount - AWAKEN_TURN;
        return Math.min(MAX_LEVEL, awakenedTurns / LEVEL_UP_TURN);
    }

    @Override
    public int visiblyUpgraded() {
        return camelliaLevel();
    }

    @Override
    public boolean doEquip(Hero hero) {
        return super.doEquip(hero);
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single) {
        if (super.doUnequip(hero, collect, single)) {
            Buff.detach(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.GoodLuckBC.class);
            Buff.detach(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BadLuckBC.class);
            return true;
        }
        return false;
    }

    @Override
    protected ArtifactBuff passiveBuff() {
        return new BlackCamelliaBuff();
    }

    public class BlackCamelliaBuff extends ArtifactBuff {

        @Override
        public int icon() {
            return BuffIndicator.NONE;
        }

        @Override
        public boolean act() {
            spend(TICK);

            if (isEquipped((Hero) target)) {
                addTurn((Hero) target);
                if (isAwakened) {
                    if (target.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BadLuckBC.class) != null) {
                        Buff.detach(target, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BadLuckBC.class);
                    }
                    Buff.affect(target, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.GoodLuckBC.class);
                } else {
                    if (target.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.GoodLuckBC.class) != null) {
                        Buff.detach(target, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.GoodLuckBC.class);
                    }
                    Buff.affect(target, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BadLuckBC.class);
                }
            } else {
                Buff.detach(target, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.GoodLuckBC.class);
                Buff.detach(target, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BadLuckBC.class);
            }

            return true;
        }
    }
    public void addTurn(Hero hero) {
        if (cursed) return;
        if (turnCount >= MAX_TURN) return;

        turnCount++;

        if (!isAwakened && turnCount >= AWAKEN_TURN) {
            isAwakened = true;
            Buff.detach(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BadLuckBC.class);
            Buff.affect(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.GoodLuckBC.class);
            com.watabou.noosa.audio.Sample.INSTANCE.play(com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.EVOKE);
            com.shatteredpixel.shatteredpixeldungeon.utils.GLog.p("검은 동백나무의 두 바늘이 8에 겹쳐졌습니다!");
        }

        updateQuickslot();
    }

    @Override
    public String desc() {
        String baseDesc = super.desc();
        if (cursed) {
            int remainingTurns = Math.max(0, AWAKEN_TURN - turnCount);
            baseDesc += "\n\n남은 턴: " + remainingTurns + "턴";
            baseDesc += "\n\n_저주받은 유물은 바늘이 움직이지 않습니다._";
        }
        return baseDesc;
    }

    @Override
    public java.util.ArrayList<String> actions(Hero hero) {
        java.util.ArrayList<String> actions = super.actions(hero);
        if (isEquipped(hero) && isAwakened) {
            actions.add(AC_ZAP);
        }
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        if (action.equals(AC_ZAP)) {
            if (isEquipped(hero)) {
                doUnequip(hero, false);
            }
            detach(hero.belongings.backpack);

            com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.BlackCamelliaSword sword =
                    new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.BlackCamelliaSword();
            sword.upgrade(this.level());

            if (!sword.collect(hero.belongings.backpack)) {
                com.shatteredpixel.shatteredpixeldungeon.Dungeon.level.drop(sword, hero.pos).sprite.drop();
            }

            com.watabou.noosa.audio.Sample.INSTANCE.play(com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.EVOKE);
            hero.sprite.operate(hero.pos);
            com.shatteredpixel.shatteredpixeldungeon.utils.GLog.p("변환 성공");

            hero.spend(1f);

        } else {
            super.execute(hero, action);
        }
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(TURN_COUNT_KEY,    turnCount);
        bundle.put(IS_AWAKENED_KEY,   isAwakened);
        bundle.put(TRIES_TO_DROP_KEY, triesToDrop);
        bundle.put(DROPS_TO_RARE_KEY, dropsToRare);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        turnCount   = bundle.getInt(TURN_COUNT_KEY);
        isAwakened  = bundle.getBoolean(IS_AWAKENED_KEY);
        triesToDrop = bundle.getFloat(TRIES_TO_DROP_KEY);
        dropsToRare = bundle.getInt(DROPS_TO_RARE_KEY);
    }
}