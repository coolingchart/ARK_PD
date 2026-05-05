package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SpiderMineSprite;
import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class SpiderMineNPC extends NPC {

    {
        spriteClass  = SpiderMineSprite.class;
        alignment    = Alignment.ALLY;
        viewDistance = 3;
        state        = WANDERING;
        WANDERING = new MineWaiting();
    }

    private boolean justSpawned = true;

    public SpiderMineNPC() {
        super();
        HP = HT = 1;
    }

    @Override
    public float speed() { return 1.0f; }

    @Override
    public int defenseSkill(Char enemy) { return 80; }

    @Override
    public int drRoll() { return 0; }

    @Override
    protected boolean act() {
        if (!isAlive()) return true;

        for (Mob mob : Dungeon.level.mobs) {
            if (mob.alignment == Alignment.ENEMY
                    && !mob.flying
                    && Dungeon.level.adjacent(pos, mob.pos)) {
                explode();
                return true;
            }
        }

        Char enemy = chooseEnemy();
        if (enemy != null) {
            if (!Dungeon.level.adjacent(pos, enemy.pos)) {
                getCloser(enemy.pos);
                spend(TICK);
            } else {
                spend(TICK);
            }
        } else {
            spend(TICK);
        }

        return true;
    }

    @Override
    protected Char chooseEnemy() {
        Char bestEnemy = null;
        int  minDist   = Integer.MAX_VALUE;

        if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()) {
            fieldOfView = new boolean[Dungeon.level.length()];
            Dungeon.level.updateFieldOfView(this, fieldOfView);
        }

        for (Mob mob : Dungeon.level.mobs) {
            if (mob.alignment == Alignment.ENEMY && !mob.flying) {
                int dist = Dungeon.level.distance(pos, mob.pos);

                // fieldOfView[mob.pos] == true → 시야 안에 있음 (LOS 통과)
                if (dist <= viewDistance && fieldOfView[mob.pos]) {
                    if (dist < minDist) {
                        minDist   = dist;
                        bestEnemy = mob;
                    }
                }
            }
        }
        return bestEnemy;
    }

    private void explode() {
        Sample.INSTANCE.play(Assets.Sounds.BLAST);

        if (Dungeon.level.heroFOV[pos]) {
            CellEmitter.center(pos).burst(BlastParticle.FACTORY, 30);
            Camera.main.shake(3, 0.2f);
        }

        for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
            int cell = pos + PathFinder.NEIGHBOURS8[i];

            if (cell >= 0 && cell < Dungeon.level.length()) {
                if (Dungeon.level.heroFOV[cell]) {
                    CellEmitter.get(cell).burst(SmokeParticle.FACTORY, 4);
                }

                Char target = Actor.findChar(cell);
                if (target != null && !target.flying) {
                    int damage = Random.NormalIntRange(30, 50);

                    // 영웅 오폭 시 0.35f 적용 → 11 ~ 18 데미지
                    if (target instanceof Hero) {
                        damage = Math.round(damage * 0.35f);
                    }
                    target.damage(damage, this);
                }
            }
        }

        // 지뢰 중심 셀 데미지 판정
        if (Dungeon.level.heroFOV[pos]) {
            CellEmitter.get(pos).burst(SmokeParticle.FACTORY, 4);
        }

        Char centerTarget = Actor.findChar(pos);
        if (centerTarget != null && !centerTarget.flying) {
            int damage = Random.NormalIntRange(30, 50);
            if (centerTarget instanceof Hero) {
                damage = Math.round(damage * 0.35f);
            }
            centerTarget.damage(damage, this);
        }

        this.sprite.die();
        this.destroy();
    }

    @Override
    public void die(Object cause) {
        super.die(cause);
    }

    @Override public int damageRoll()              { return 0; }
    @Override public int attackSkill(Char target)  { return 0; }

    @Override
    public boolean moveSprite(int oldPos, int newPos) {
        boolean success = super.moveSprite(oldPos, newPos);
        if (success) {
            CellEmitter.get(oldPos).burst(Speck.factory(Speck.STEAM), 2);
            CellEmitter.get(newPos).burst(Speck.factory(Speck.LIGHT), 1);
        }
        return success;
    }
    private class MineWaiting extends Mob.Wandering {

        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            if (enemyInFOV) {
                enemySeen = true;
                notice();
                alerted = true;
                state   = HUNTING;
                target  = enemy.pos;
            } else {
                enemySeen = false;
                spend(TICK); // 제자리 대기
            }
            return true;
        }
    }
}
