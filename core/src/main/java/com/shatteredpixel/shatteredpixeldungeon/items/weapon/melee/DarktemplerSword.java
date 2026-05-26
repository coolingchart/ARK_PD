/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArcaneArmor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.SandalsOfNature;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class DarktemplerSword extends MeleeWeapon {

	{
		image = ItemSpriteSheet.DarktemplerSword;
		hitSound = Assets.Sounds.DARKTEMPLERHIT;
		hitSoundPitch = 1f;

		tier = 5;
	}

	@Override
	public int max(int lvl) {
		return  5*(tier+4) + lvl*(tier);   //scaling unchanged
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
			multiplier = 0.8f;
		}

		int finalMax = Math.round(baseMax * multiplier);
		int finalMin;

		if (multiplier >= 1.1f) {
			finalMin = Math.round(finalMax * 0.32f);
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
				multiplier = 0.8f;
			}

			int finalMax = Math.round(baseMax * multiplier);
			int finalMin;

			if (multiplier >= 1.1f) {
				finalMin = Math.round(finalMax * 0.32f);
			} else {
				finalMin = Math.min(baseMin, finalMax);
			}

			int percent = Math.round(multiplier * 100);
			return originalDesc + "\n\n현재 데미지 배율: +" + percent + "%\n현재 데미지: " + finalMin + " ~ " + finalMax;
		}
		return originalDesc;
	}

	@Override
	public int STRReq(int lvl) {
		return STRReq(tier+1, lvl); //20 base strength req, up from 18
	}
}