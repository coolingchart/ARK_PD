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

	// 액션 태그 정의
	public static final String AC_ACTIVATE = "ACTIVATE"; // 각성 버튼
	public static final String AC_LEAP     = "LEAP";     // 도약 버튼

	// SP(기력) 시스템 설정
	public int arts = 0;              // 현재 SP
	public int artschargeCap = 100;   // 최대 SP (100 충전 시 사용 가능)

	// 각성 지속 시간 설정
	public int awakeTurns = 0;               // 남은 각성 턴
	private static final int MAX_AWAKE_TURNS = 40; // 최대 각성 시간 (40턴으로 설정)

	{
		image = ItemSpriteSheet.Hinagiku;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 1f;
		tier = 5;
	}

	// 무기 기본 데미지 공식
	@Override
	public int max(int lvl) {
		return 5 * (tier) + 2 + lvl * (tier + 1);
	}

	@Override
	public String desc() {
        return "_\"괴롭고 힘들고 죽을 것 같은 마음 너머에는 무엇과도 바꿀 수 없는 진정한 기쁨이 있는 법이야.\"_\n\n쿠로츠바키에 필적하는 전설의 명검으로 카츠라 히나기쿠의 힘이 깃들어 있기 때문에 검을 능숙하게 다룰 수 있게 되지만 _부유 시 고소공포증으로 인한 약화 효과를 얻습니다._\n\n_1턴이 지날때마다 SP를 채우고 SP가 100이 되면 특수 스킬을 사용할 수 있습니다._";
    }

	// 타이머 버프 생존 확인 (세이브/로드 시 복구 로직)
	private void checkBuff() {
		if (Dungeon.hero != null && isEquipped(Dungeon.hero)) {
			if (Dungeon.hero.buff(SPTimer.class) == null) {
				Buff.affect(Dungeon.hero, SPTimer.class);
			}
		}
	}

	// 전투 중 추가 효과 처리 (도약 공격 데미지 증폭)
	// 전투 중 추가 효과 처리 (각성 증폭 및 도약 증폭 분리)
	@Override
	public int proc(Char attacker, Char defender, int damage) {
		LeapDamage buff = attacker.buff(LeapDamage.class);

		if (buff != null) {
			// 1. 도약 공격인 경우: 각성 상태와 무관하게 무조건 1.5배로 고정
			damage = (int)(damage * 1.5f);
			attacker.sprite.showStatus(0xFF0000, ""); // 붉은 이펙트 표시
			buff.detach();
		} else if (awakeTurns > 0) {
			// 2. 도약 공격이 아닌 일반 타격이면서 각성 상태인 경우: 1.25배 적용
			damage = (int)(damage * 1.25f);
		}

		return super.proc(attacker, defender, damage);
	}

	// 상태창 및 퀵슬롯에 SP 수치 표시
	@Override
	public String status() {
		checkBuff();
		if (awakeTurns > 0) return "0/" + artschargeCap; // 각성 중엔 SP 0 고정 표시
		return arts + "/" + artschargeCap;
	}

	// 인벤토리에서 보여줄 액션 목록
	@Override
	public ArrayList<String> actions(Hero hero) {
		checkBuff();
		ArrayList<String> actions = super.actions(hero);
		if (isEquipped(hero)) {
			if (awakeTurns > 0) {
				actions.add(AC_LEAP); // 각성 중엔 도약 가능
			} else {
				actions.add(AC_ACTIVATE); // 비각성 시 각성 버튼 표시
			}
		}
		return actions;
	}

	// 선택한 액션 실행 로직
	@Override
	public void execute(final Hero hero, String action) {
		if (action.equals(AC_ACTIVATE)) {
			// SP 100 소모 체크
			if (arts < artschargeCap) {
				GLog.w("SP가 부족합니다");
				return;
			}

			arts = 0; // SP 초기화
			awakeTurns = MAX_AWAKE_TURNS; // 40턴 각성 시작
			updateQuickslot();

			// 각성 버프 부여 (축복 + 빛)
			Buff.affect(hero, Bless.class, MAX_AWAKE_TURNS);
			Buff.affect(hero, Light.class, MAX_AWAKE_TURNS);

			Sample.INSTANCE.play(Assets.Sounds.SKILL_BASIC);
			hero.spend(1f); // 1턴 소모
			hero.next();

		} else if (action.equals(AC_LEAP)) {
			// 도약 위치 선택창 띄우기
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

	// 도약 상세 로직
	private void performLeap(Hero hero, int targetPos) {
		Level level = Dungeon.level;

		if (hero.rooted) {
			GLog.w("속박되어 도약할 수 없습니다!");
			return;
		}

		// 도약 전용 판정 배열 (문, 거미줄 통과 허용 / 낭떠러지, 벽 차단)
		boolean[] leapable = new boolean[level.length()];
		Blob webBlob = level.blobs.get(Web.class);
		for (int i = 0; i < level.length(); i++) {
			leapable[i] = !level.solid[i] || level.map[i] == Terrain.DOOR || (webBlob != null && webBlob.cur[i] > 0);
		}

		PathFinder.buildDistanceMap(hero.pos, leapable, 3);

		int oldPos = hero.pos;
		Char enemy = Actor.findChar(targetPos);

		if (enemy != null && enemy != hero) {

			//적과의 거리가 1칸 이하(3x3 이내)라면 도약 공격 취소
			if (level.distance(hero.pos, targetPos) <= 1) {
				GLog.w("거리가 너무 가깝습니다");
				return;
			}

			int dest = -1;
			for (int i : PathFinder.NEIGHBOURS8) {
				int cell = targetPos + i;

				if (cell < 0 || cell >= level.length()) continue;
				if (Actor.findChar(cell) != null) continue;
				// 낭떠러지나 밟을 수 없는 곳은 착지 불가
				if (!level.passable[cell] && level.map[cell] != Terrain.DOOR && (webBlob == null || webBlob.cur[cell] == 0)) continue;

				if (dest == -1 || PathFinder.distance[dest] > PathFinder.distance[cell]) {
					dest = cell;
				} else if (PathFinder.distance[dest] == PathFinder.distance[cell]) {
					if (level.trueDistance(hero.pos, dest) > level.trueDistance(hero.pos, cell)) {
						dest = cell;
					}
				}
			}

			//메시지 2개로 분리
			if (dest == -1 || PathFinder.distance[dest] == Integer.MAX_VALUE) {
				// 3칸보다 멀어서 못 가는 경우
				if (level.distance(hero.pos, targetPos) > 3) {
					GLog.w("거리가 너무 멉니다.");
				} else {
					// 거리는 닿지만 주변이 벽/낭떠러지로 막혀서 못 가는 경우
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

			//메시지 다르게 출력
		} else if (Actor.findChar(targetPos) == null) {

			// 1순위: 밟을 수 없는 지형(벽, 낭떠러지)인지 먼저 확인
			if (!leapable[targetPos] || (!level.passable[targetPos] && level.map[targetPos] != Terrain.DOOR && (webBlob == null || webBlob.cur[targetPos] == 0))) {
				GLog.w("그곳으로는 도약할 수 없습니다.");
				return;
			}

			// 2순위: 지형은 문제없지만 3칸 거리를 벗어났는지 확인
			if (PathFinder.distance[targetPos] == Integer.MAX_VALUE) {
				GLog.w("거리가 너무 멉니다.");
				return;
			}

			// 도약 실행
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

	// 데이터 저장
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("arts", arts);
		bundle.put("awakeTurns", awakeTurns);
	}

	// 데이터 불러오기
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		arts = bundle.getInt("arts");
		awakeTurns = bundle.getInt("awakeTurns");
	}

	// 장착 시 처리
	@Override
	public boolean doEquip(Hero hero) {
		super.doEquip(hero);
		checkBuff();
		return false;
	}

	// 장착 해제 시 처리 (모든 연동 버프 제거)
	@Override
	public boolean doUnequip(Hero hero, boolean rm) {
		super.doUnequip(hero, rm);
		Buff.detach(hero, SPTimer.class);
		Buff.detach(hero, Light.class);
		Buff.detach(hero, Weakness.class);
		return rm;
	}

	// 도약 공격 증폭 버프 정의
	public static class LeapDamage extends Buff {}

	// 실시간 SP 충전 및 각성 관리 타이머
	// 실시간 SP 충전 및 각성 관리 타이머
	public static class SPTimer extends Buff {
		@Override
		public boolean act() {
			if (target instanceof Hero) {
				Hero hero = (Hero)target;
				if (hero.belongings.weapon instanceof Hinagiku) {
					Hinagiku h = (Hinagiku)hero.belongings.weapon;

					//부유 시 약화 100턴 고정 유지
					if (hero.buff(Levitation.class) != null) {
						if (hero.buff(Weakness.class) == null) {
							// 처음 부유를 시작할 때 100턴을 꽉 채워줍니다.
							Buff.affect(hero, Weakness.class, 100f);
						} else {
							// 이미 약화가 있다면, 이번 턴에 소모된 1턴(1f)만큼만 다시 더해줍니다.
							// 결과적으로 약화 턴 수가 항상 100턴으로 고정됩니다.
							Buff.affect(hero, Weakness.class, 1f);
						}
					} else {
						// 땅에 닿으면 약화를 즉시 지워버립니다.
						Buff.detach(hero, Weakness.class);
					}

					// 각성 상태 관리
					if (h.awakeTurns > 0) {
						h.awakeTurns--;
						h.arts = 0; // 각성 중엔 기력 충전 차단

						if (h.awakeTurns <= 0) {
							h.awakeTurns = 0;
							Buff.detach(hero, Light.class); // 각성 종료 시 시야 버프 해제
						}
						h.updateQuickslot();
					}
					// 비각성 시 매 턴 기력 1씩 충전
					else if (h.arts < h.artschargeCap) {
						h.arts++;
						if (h.arts >= h.artschargeCap) {
							h.arts = h.artschargeCap;
						}
						h.updateQuickslot();
					}
				} else {
					detach(); // 무기 해제 시 타이머 종료
				}
			} else {
				detach();
			}
			spend(TICK); // 1턴 주기
			return true;
		}
	}
}