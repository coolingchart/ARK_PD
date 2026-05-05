package com.shatteredpixel.shatteredpixeldungeon.actors;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.SpiderMineNPC;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;

public class SpiderMinePlanted extends Actor {

    public int pos;
    private ItemSprite visualSprite;


    public SpiderMinePlanted() {}

    public SpiderMinePlanted(int pos) {
        this.pos = pos;
    }

    public void spawnVisualSprite() {  // private → public
        if (visualSprite != null && visualSprite.parent != null) return; // 중복 생성 방지

        visualSprite = new ItemSprite() {
            @Override
            public void update() {
                super.update();
                if (Dungeon.level != null && Dungeon.level.heroFOV != null) {
                    this.visible = Dungeon.level.heroFOV[SpiderMinePlanted.this.pos];
                }
            }
        };
        visualSprite.view(ItemSpriteSheet.SPIDER_BOMB, null);
        visualSprite.place(pos);
        Game.scene().add(visualSprite);
    }

    @Override
    protected boolean act() {
        if (visualSprite == null || visualSprite.parent == null) {
            // GameScene이 완전히 준비됐는지 확인
            if (Game.scene() instanceof GameScene) {
                spawnVisualSprite();
            }
        }

        for (Mob mob : Dungeon.level.mobs) {
            if (mob.alignment == Char.Alignment.ENEMY && !mob.flying
                    && Dungeon.level.distance(pos, mob.pos) <= 2) {

                Sample.INSTANCE.play(Assets.Sounds.SPMINEACT);
                SpiderMineNPC activeMine = new SpiderMineNPC();
                activeMine.pos = this.pos;
                Dungeon.level.mobs.add(activeMine);
                Actor.add(activeMine);

                try {
                    activeMine.sprite = activeMine.spriteClass.newInstance();
                    activeMine.sprite.link(activeMine);
                    activeMine.sprite.place(activeMine.pos);
                    Game.scene().add(activeMine.sprite);
                } catch (Exception e) {}

                destroy();
                return true;
            }
        }

        spend(TICK);
        return true;
    }

    public void destroy() {
        if (visualSprite != null) {
            visualSprite.killAndErase();
        }
        Actor.remove(this);
    }

    private static final String POS = "pos";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(POS, pos);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        pos = bundle.getInt(POS);
    }
}