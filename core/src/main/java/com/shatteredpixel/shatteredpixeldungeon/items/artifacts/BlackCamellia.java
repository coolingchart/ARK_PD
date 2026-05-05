package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

public class BlackCamellia extends Artifact {

    // --- 부유함 보너스 게이지 저장 변수 (증발 방지용) ---
    public float triesToDrop = Float.MIN_VALUE;
    public int dropsToRare = Integer.MIN_VALUE;

    // --- 각성 관련 변수 ---
    public int turnCount = 0;
    public boolean isAwakened = false;

    private static final String TURN_COUNT_KEY   = "turnCount";
    private static final String IS_AWAKENED_KEY  = "isAwakened";
    private static final String TRIES_TO_DROP_KEY = "triesToDrop";
    private static final String DROPS_TO_RARE_KEY = "dropsToRare";
    private static final String AC_ZAP = "ZAP";

    {
        image = ItemSpriteSheet.ARTIFACT_BlackCamellia;
    }

    // --- 1. 장착 시 버프 부착 ---
    @Override
    public boolean doEquip(Hero hero) {
        if (super.doEquip(hero)) {
            if (isAwakened) {
                Buff.affect(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.GoodLuckBC.class);
            } else {
                Buff.affect(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BadLuckBC.class);
            }
            return true;
        }
        return false;
    }

    // --- 2. 패시브 버프: 장착 여부에 따라 버프 자동 관리 ---
    @Override
    protected ArtifactBuff passiveBuff() {
        return new BlackCamelliaBuff();
    }

    public class BlackCamelliaBuff extends ArtifactBuff {
        @Override
        public boolean act() {
            spend(TICK);

            if (isEquipped((Hero) target)) {
                // 장착 중: 각성 상태에 맞는 버프 유지
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
                // 장착 해제: 양쪽 버프 모두 제거
                Buff.detach(target, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.GoodLuckBC.class);
                Buff.detach(target, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BadLuckBC.class);
            }

            return true;
        }
    }

    // --- 3. 턴 경과 및 각성 처리 (Hero.java의 spend에서 호출됨) ---
    public void addTurn() {
        if (isAwakened) return;

        turnCount++;
        if (turnCount >= 1600) {
            isAwakened = true;

            Buff.detach(curUser, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BadLuckBC.class);
            Buff.affect(curUser, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.GoodLuckBC.class);

            com.watabou.noosa.audio.Sample.INSTANCE.play(com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.EVOKE);
            com.shatteredpixel.shatteredpixeldungeon.utils.GLog.p("검은 동백나무의 두 바늘이 8에 겹쳐졌습니다!");

            updateQuickslot();
        }
    }

    // --- 4. 설명창 텍스트 수정 (남은 턴 표시) ---
    @Override
    public String desc() {
        String baseDesc = super.desc();
        if (isAwakened) {
            return baseDesc;
        } else {
            int remainingTurns = Math.max(0, 1600 - turnCount);
            return baseDesc + "\n\n남은 턴: " + remainingTurns + "턴";
        }
    }

    // --- 5. 퀵슬롯 상태창 표시 ---
    @Override
    public String status() {
        return null;
    }

    // --- 6. 액션 및 변환 ---
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
                doUnequip(hero, true);
            }

            // 새로운 무기 생성
            com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.BlackCamelliaSword sword =
                    new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.BlackCamelliaSword();

            // 유물 레벨 이전
            sword.upgrade(this.level());

            // 기존 유물 파괴
            detach(hero.belongings.backpack);

            // 새로운 무기 인벤토리에 지급
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

    // --- 세이브 / 로드 ---
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