package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Web;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.shatteredpixel.shatteredpixeldungeon.utils.BArray;

import java.util.ArrayList;

public class Hinagiku extends MeleeWeapon {

	public static final String AC_ACTIVATE = "ACTIVATE";
	public static final String AC_LEAP     = "LEAP";

	public int arts = 0;
	public int artschargeCap = 100;
	public int awakeTurns = 0;
	private static final int MAX_AWAKE_TURNS = 40;

	{
		image = ItemSpriteSheet.Hinagiku;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 1f;
		tier = 5;
	}

	@Override
	public int max(int lvl) {
		return 5 * (tier) + 2 + lvl * (tier + 1);
	}

	@Override
	public String desc() {
        return "_\"괴롭고 힘들고 죽을 것 같은 마음 너머에는 무엇과도 바꿀 수 없는 진정한 기쁨이 있는 법이야.\"_\n\n쿠로츠바키에 필적하는 전설의 명검으로 카츠라 히나기쿠의 힘이 깃들어 있기 때문에 검을 능숙하게 다룰 수 있게 되지만 _부유 시 고소공포증으로 인한 약화 효과를 얻습니다._\n\n_1턴이 지날때마다 SP를 채우고 SP가 100이 되면 특수 스킬을 사용할 수 있습니다._";
    }

	private void checkBuff() {
		if (Dungeon.hero != null && isEquipped(Dungeon.hero)) {
			if (Dungeon.hero.buff(SPTimer.class) == null) {
				Buff.affect(Dungeon.hero, SPTimer.class);
			}
		}
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		LeapDamage buff = attacker.buff(LeapDamage.class);

		if (buff != null) {
			damage = (int)(damage * 1.5f);
			attacker.sprite.showStatus(0xFF0000, "");
			buff.detach();
		} else if (awakeTurns > 0) {
			damage = (int)(damage * 1.25f);
		}

		return super.proc(attacker, defender, damage);
	}

	@Override
	public String status() {
		checkBuff();
		if (awakeTurns > 0) return "0/" + artschargeCap;
		return arts + "/" + artschargeCap;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		checkBuff();
		ArrayList<String> actions = super.actions(hero);
		if (isEquipped(hero)) {
			if (awakeTurns > 0) {
				actions.add(AC_LEAP);
			} else {
				actions.add(AC_ACTIVATE);
			}
		}
		return actions;
	}

	@Override
	public void execute(final Hero hero, String action) {
		if (action.equals(AC_ACTIVATE)) {
			// SP 100 소모 체크
			if (arts < artschargeCap) {
				GLog.w("SP가 부족합니다");
				return;
			}

			arts = 0;
			awakeTurns = MAX_AWAKE_TURNS;
			updateQuickslot();

			Buff.affect(hero, Bless.class, MAX_AWAKE_TURNS);
			Buff.affect(hero, Light.class, MAX_AWAKE_TURNS);

			Sample.INSTANCE.play(Assets.Sounds.SKILL_BASIC);
			hero.spend(1f);
			hero.next();

		} else if (action.equals(AC_LEAP)) {
			GameScene.selectCell(new CellSelector.Listener() {
				@Override
				public void onSelect(Integer targetPos) {
					if (targetPos != null) {
						performLeap(hero, targetPos);
					}
				}
				@Override
				public String prompt() {
					return "도약 위치를 선택하세요";
				}
			});
		} else {
			super.execute(hero, action);
		}
	}


	private void performLeap(Hero hero, int targetPos) {
		Level level = Dungeon.level;

		if (hero.rooted) {
			GLog.w("속박되어 도약할 수 없습니다!");
			return;
		}

		boolean[] leapable = new boolean[level.length()];
		Blob webBlob = level.blobs.get(Web.class);
		for (int i = 0; i < level.length(); i++) {
			leapable[i] = !level.solid[i] || level.map[i] == Terrain.DOOR || (webBlob != null && webBlob.cur[i] > 0);
		}

		PathFinder.buildDistanceMap(hero.pos, leapable, 3);

		int oldPos = hero.pos;
		Char enemy = Actor.findChar(targetPos);

		if (enemy != null && enemy != hero) {

			if (level.distance(hero.pos, targetPos) <= 1) {
				GLog.w("거리가 너무 가깝습니다");
				return;
			}

			int dest = -1;
			for (int i : PathFinder.NEIGHBOURS8) {
				int cell = targetPos + i;

				if (cell < 0 || cell >= level.length()) continue;
				if (Actor.findChar(cell) != null) continue;
				if (!level.passable[cell] && level.map[cell] != Terrain.DOOR && (webBlob == null || webBlob.cur[cell] == 0)) continue;

				if (dest == -1 || PathFinder.distance[dest] > PathFinder.distance[cell]) {
					dest = cell;
				} else if (PathFinder.distance[dest] == PathFinder.distance[cell]) {
					if (level.trueDistance(hero.pos, dest) > level.trueDistance(hero.pos, cell)) {
						dest = cell;
					}
				}
			}


			if (dest == -1 || PathFinder.distance[dest] == Integer.MAX_VALUE) {

				if (level.distance(hero.pos, targetPos) > 3) {
					GLog.w("거리가 너무 멉니다.");
				} else {

					GLog.w("그곳으로는 도약할 수 없습니다.");
				}
				return;
			}

			hero.pos = dest;
			level.occupyCell(hero);
			Dungeon.observe();
			hero.checkVisibleMobs();

			hero.sprite.place(hero.pos);
			hero.sprite.turnTo(hero.pos, targetPos);

			CellEmitter.get(oldPos).burst(Speck.factory(Speck.WOOL), 6);
			CellEmitter.get(hero.pos).burst(Speck.factory(Speck.WOOL), 6);

			Sample.INSTANCE.play(Assets.Sounds.HIT_SLASH);
			Buff.affect(hero, LeapDamage.class);
			hero.curAction = new HeroAction.Attack(enemy);
			hero.next();


		} else if (Actor.findChar(targetPos) == null) {


			if (!leapable[targetPos] || (!level.passable[targetPos] && level.map[targetPos] != Terrain.DOOR && (webBlob == null || webBlob.cur[targetPos] == 0))) {
				GLog.w("그곳으로는 도약할 수 없습니다.");
				return;
			}


			if (PathFinder.distance[targetPos] == Integer.MAX_VALUE) {
				GLog.w("거리가 너무 멉니다.");
				return;
			}


			hero.pos = targetPos;
			level.occupyCell(hero);
			hero.sprite.move(oldPos, targetPos);

			CellEmitter.get(oldPos).burst(Speck.factory(Speck.WOOL), 6);
			CellEmitter.get(targetPos).burst(Speck.factory(Speck.WOOL), 6);

			Sample.INSTANCE.play(Assets.Sounds.MISS);
			hero.spend(0.5f);
			hero.next();
			Dungeon.observe();
		}
	}


	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("arts", arts);
		bundle.put("awakeTurns", awakeTurns);
	}


	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		arts = bundle.getInt("arts");
		awakeTurns = bundle.getInt("awakeTurns");
	}


	@Override
	public boolean doEquip(Hero hero) {
		super.doEquip(hero);
		checkBuff();
		return false;
	}


	@Override
	public boolean doUnequip(Hero hero, boolean rm) {
		super.doUnequip(hero, rm);
		Buff.detach(hero, SPTimer.class);
		Buff.detach(hero, Light.class);
		Buff.detach(hero, Weakness.class);
		return rm;
	}


	public static class LeapDamage extends Buff {}


	public static class SPTimer extends Buff {
		@Override
		public boolean act() {
			if (target instanceof Hero) {
				Hero hero = (Hero)target;
				if (hero.belongings.weapon instanceof Hinagiku) {
					Hinagiku h = (Hinagiku)hero.belongings.weapon;


					if (hero.buff(Levitation.class) != null) {
						if (hero.buff(Weakness.class) == null) {

							Buff.affect(hero, Weakness.class, 100f);
						} else {
							Buff.affect(hero, Weakness.class, 1f);
						}
					} else {
						Buff.detach(hero, Weakness.class);
					}

					if (h.awakeTurns > 0) {
						h.awakeTurns--;
						h.arts = 0;

						if (h.awakeTurns <= 0) {
							h.awakeTurns = 0;
							Buff.detach(hero, Light.class);
						}
						h.updateQuickslot();
					}
					else if (h.arts < h.artschargeCap) {
						h.arts++;
						if (h.arts >= h.artschargeCap) {
							h.arts = h.artschargeCap;
						}
						h.updateQuickslot();
					}
				} else {
					detach();
				}
			} else {
				detach();
			}
			spend(TICK);
			return true;
		}
	}
}