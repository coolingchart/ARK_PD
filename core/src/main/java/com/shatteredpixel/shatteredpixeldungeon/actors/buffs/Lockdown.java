/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.Image; //  Sprite 대신 엔진의 정규 부품인 Image 수입

public class Lockdown extends FlavourBuff {

	private Emitter emitter;
	private Image lockdownOverlay; //껍데기도 Image로 선언

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	@Override
	public boolean attachTo( Char target ) {
		if (super.attachTo( target )) {
			target.paralysed++;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void detach() {
		super.detach();
		if (target.paralysed > 0)
			target.paralysed--;

		if (target.sprite != null) {
			if (target.paralysed == 0) {
				target.sprite.remove(CharSprite.State.PARALYSED);
			}

			if (emitter != null) emitter.on = false;
			if (lockdownOverlay != null) {
				target.sprite.parent.remove(lockdownOverlay);
				lockdownOverlay = null;
			}
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.LOCKDOWN;
	}

	@Override
	public void fx(boolean on) {
		if (on) {
			target.sprite.add(CharSprite.State.PARALYSED);

			// Image 클래스를 사용해 도플갱어 껍데기를 만듭니다
			lockdownOverlay = new Image() {
				@Override
				public void update() {
					super.update();
					if (target != null && target.sprite != null) {
						this.texture(target.sprite.texture);
						this.frame(target.sprite.frame());
						this.x = target.sprite.x;
						this.y = target.sprite.y;
						this.flipHorizontal = target.sprite.flipHorizontal;

						this.tint(0xCCCCCC, 0.4f);
					}
				}
			};
			target.sprite.parent.add(lockdownOverlay);

			emitter = target.sprite.centerEmitter();
			emitter.start(SparkParticle.FACTORY, 0.15f, 0);

		} else {
			if (target.paralysed <= 1) target.sprite.remove(CharSprite.State.PARALYSED);

			if (emitter != null) emitter.on = false;
			if (lockdownOverlay != null) {
				target.sprite.parent.remove(lockdownOverlay);
				lockdownOverlay = null;
			}
		}
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