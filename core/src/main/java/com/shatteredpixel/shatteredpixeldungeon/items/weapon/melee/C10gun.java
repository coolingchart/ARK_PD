package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Lockdown;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class C10gun extends MeleeWeapon {
    public static final String AC_ZAP = "ZAP";

    {
        image = ItemSpriteSheet.C10GUN;
        hitSound = Assets.Sounds.GHOSTSHOOT;
        hitSoundPitch = 1f;

        tier = 5;
        RCH = 3;

        usesTargeting = true;
        defaultAction = AC_ZAP;

    }


    protected int collisionProperties = Ballistica.MAGIC_BOLT;

    private int arts = 3;
    private int artschargeCap = 3;

    private int hitCount = 0;

    @Override
    public int min(int lvl) {
        return 8+lvl;
    }

    @Override
    public int max(int lvl) {
        return  4 * (tier+2) +
                lvl*(tier);
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {

        if (arts < artschargeCap) {
            hitCount++;
            if (hitCount >= 6) {
                SPCharge(1);
                hitCount = 0;
            }
        }
        return super.proc(attacker, defender, damage);
    }

    public void SPCharge(int n) {
        arts += n;
        if (artschargeCap < arts) arts = artschargeCap;
        updateQuickslot();
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_ZAP);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_ZAP) && arts > 0) {
            if (!this.cursed) {
                cursedKnown = true;
                GameScene.selectCell(zapper);
            } else {
                Buff.affect(Dungeon.hero, Burning.class).reignite(Dungeon.hero, 4f);
                cursedKnown = true;
                arts -= 1;
            }
        }
    }

    public String statsInfo() {
        return Messages.get(this, "stats_desc", 2 + buffedLvl(), 11 + buffedLvl() * 2);
    }

    @Override
    public String status() {
        return arts + "/" + artschargeCap;
    }

    private static final String CHARGE = "arts";
    private static final String HIT_COUNT = "hitCount";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(CHARGE, arts);
        bundle.put(HIT_COUNT, hitCount);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        if (artschargeCap > 0) arts = Math.min(artschargeCap, bundle.getInt(CHARGE));
        else arts = bundle.getInt(CHARGE);
        hitCount = bundle.getInt(HIT_COUNT);
    }

    protected static CellSelector.Listener zapper = new CellSelector.Listener() {
        @Override
        public void onSelect(Integer target) {
            if (target != null) {
                final C10gun ss;
                if (curItem instanceof C10gun) {
                    ss = (C10gun) C10gun.curItem;

                    Ballistica shot = new Ballistica(curUser.pos, target, Ballistica.PROJECTILE);
                    int cell = shot.collisionPos;

                    if (target == curUser.pos || cell == curUser.pos) {
                        GLog.i(Messages.get(C10gun.class, "self_target"));
                        return;
                    }

                    curUser.sprite.zap(cell);

                    if (Actor.findChar(target) != null)
                        QuickSlotButton.target(Actor.findChar(target));
                    else
                        QuickSlotButton.target(Actor.findChar(cell));

                    if (ss.tryToZap(curUser, target)) {
                        ss.fx(shot, new Callback() {
                            public void call() {
                                ss.onZap(shot);
                            }
                        });
                    }
                }
            }
        }

        @Override
        public String prompt() {
            return Messages.get(C10gun.class, "prompt");
        }
    };

    protected void fx(Ballistica bolt, Callback callback) {
        MagicMissile.boltFromChar(curUser.sprite.parent,
                MagicMissile.MAGIC_MISSILE,
                curUser.sprite,
                bolt.collisionPos,
                callback);
        Sample.INSTANCE.play(Assets.Sounds.LOCKDOWN);
    }

    public boolean tryToZap(Hero owner, int target) {
        if (owner.buff(MagicImmune.class) != null) {
            GLog.w(Messages.get(this, "no_magic"));
            return false;
        }

        if (arts >= 1) {
            return true;
        } else {
            GLog.w(Messages.get(this, "fizzles"));
            return false;
        }
    }

    protected void onZap(Ballistica bolt) {
        Char ch = Actor.findChar(bolt.collisionPos);
        if (ch != null) {

            float duration = 4f;

            boolean isBoss = ch.properties().contains(Char.Property.BOSS);
            boolean isMiniBoss = ch.getClass().getName().contains("miniboss");
            if (isBoss || isMiniBoss) {
                duration = 1f;
            }

            Buff.affect(ch, Lockdown.class, duration);

            Sample.INSTANCE.play(Assets.Sounds.DEBUFF, 1, Random.Float(0.87f, 1.15f));
            ch.sprite.burst(0xFFB2D6FF, buffedLvl() / 2 + 2);

        } else {
            Dungeon.level.pressCell(bolt.collisionPos);
        }

        arts -= 1;
        updateQuickslot();
        curUser.spendAndNext(1f);
    }
}


