package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

public class BlackCamelliaSword extends MeleeWeapon {

	private static final String AC_DEVOUR = "DEVOUR";

	private static final String ARTS_KEY = "arts";
	private static final String DEVOUR_ACTIVE_KEY = "devourActive";
	private static final String HIT_COUNT_KEY = "hitCount";

	public static final int MAX_ARTS = 10;
	private int arts = 0;
	private int hitCount = 0;
	private boolean isDevourActive = false;

	{
		image = ItemSpriteSheet.BlackCamelliaSword;
		hitSound = Assets.Sounds.HIT_SWORD2;
		hitSoundPitch = 1f;

		tier = 5;
	}

	@Override
	public int max(int lvl) {
		return  5*(tier) + 2 +    //27 + 6
				lvl*(tier+1);
	}

	@Override
	public java.util.ArrayList<String> actions(Hero hero) {
		java.util.ArrayList<String> actions = super.actions(hero);
		if (isEquipped(hero)) {
			actions.add(AC_DEVOUR);
		}
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		if (action.equals(AC_DEVOUR)) {
			if (isDevourActive) {
				com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(Messages.get(this, "msg_already_active"));
			} else if (arts >= 3) {
				arts -= 3;
				isDevourActive = true;

				Buff.affect(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Soulchange.class);

				Sample.INSTANCE.play(Assets.Sounds.HIT_RINGOUT);

				com.shatteredpixel.shatteredpixeldungeon.utils.GLog.p(Messages.get(this, "msg_devour_ready"));
				hero.spend(1f);
				updateQuickslot();
			} else {
				com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(Messages.get(this, "msg_no_mana"));
			}
		} else {
			super.execute(hero, action);
		}
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		int finalDamage = super.proc(attacker, defender, damage);

		if (!isDevourActive && arts < MAX_ARTS) {
			hitCount++;
			if (hitCount >= 4) {
				arts++;
				hitCount = 0;
				updateQuickslot();
			}
		}

		if (isDevourActive && (defender.HP <= finalDamage || defender.HP <= 0)) {
			Hero hero = (Hero) attacker;

			// 최대 체력만큼 회복
			int healAmount = hero.HT;

			hero.HP += healAmount;
			if (hero.HP > hero.HT) hero.HP = hero.HT;

			Sample.INSTANCE.play(Assets.Sounds.HIT_SLASH);

			hero.sprite.showStatus(0x00FF00, "" + healAmount);

			isDevourActive = false;
			Buff.detach(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Soulchange.class);
		}

		return finalDamage;
	}

	@Override
	public String status() {
		return arts + "/" + MAX_ARTS;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(ARTS_KEY, arts);
		bundle.put(DEVOUR_ACTIVE_KEY, isDevourActive);
		bundle.put(HIT_COUNT_KEY, hitCount);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		arts = Math.min(MAX_ARTS, bundle.getInt(ARTS_KEY));
		isDevourActive = bundle.getBoolean(DEVOUR_ACTIVE_KEY);
		hitCount = bundle.getInt(HIT_COUNT_KEY);
	}
}