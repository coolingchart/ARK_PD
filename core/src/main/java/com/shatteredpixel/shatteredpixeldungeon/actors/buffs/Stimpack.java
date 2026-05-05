package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class Stimpack extends FlavourBuff {

	@Override
	public boolean attachTo(Char target) {

		//스팀팩만의 피 깎기을 추가로 실행
		int damage = Math.round(target.HT * 0.25f);
		if (target.HP <= damage) {
			GLog.w("체력이 부족합니다");
			return false;
		}

		if (super.attachTo(target)) {
			target.HP -= damage;
			target.sprite.showStatus(CharSprite.NEGATIVE, Integer.toString(damage));
			Sample.INSTANCE.play(Assets.Sounds.STIMPACK);

			return true;
		}
		return false;
	}

	// 버프가 끝날 때 최대 체력의 10%를 회복
	@Override
	public void detach() {
		if (target != null && target.isAlive()) {

			int heal = Math.round(target.HT * 0.15f);
			target.HP = Math.min(target.HP + heal, target.HT);

			if (target.sprite != null) {
				target.sprite.showStatus(CharSprite.POSITIVE, "" + heal);
			}
		}

		super.detach();
	}
	// ==========================================

	@Override
	public int icon() {
		return BuffIndicator.STIMPACK;
	}

	@Override
	public String toString() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", dispTurns());
	}
}