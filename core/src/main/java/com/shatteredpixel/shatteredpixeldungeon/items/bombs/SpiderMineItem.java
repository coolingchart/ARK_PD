package com.shatteredpixel.shatteredpixeldungeon.items.bombs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.SpiderMinePlanted;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant; // 🌟 Plant 클래스를 임포트합니다.
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class SpiderMineItem extends Item {

    public static final String AC_ZAP = "ZAP";

    {
        image = ItemSpriteSheet.SPIDER_BOMB;
        stackable = true;
        defaultAction = AC_ZAP;
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_ZAP);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        if (action.equals(AC_ZAP)) {

            int plantPos = hero.pos;
            SpiderMinePlanted plantedMine = new SpiderMinePlanted(plantPos);
            Actor.add(plantedMine);

            Sample.INSTANCE.play(Assets.Sounds.SPMINESET);

            detach(hero.belongings.backpack);
            hero.spendAndNext(1.0f);
        } else {
            super.execute(hero, action);
        }
    }

    public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe {

        @Override
        public boolean testIngredients(ArrayList<Item> ingredients) {
            int runestoneCount = 0;
            int seedCount = 0;

            for (Item item : ingredients) {
                if (!item.isIdentified()) return false; // ← 이 줄 추가

                if (item instanceof Runestone) {
                    runestoneCount += item.quantity();
                } else if (item instanceof Plant.Seed) {
                    seedCount += item.quantity();
                } else {
                    return false;
                }
            }
            return runestoneCount >= 2 && seedCount >= 1;
        }

        @Override
        public int cost(java.util.ArrayList<Item> ingredients) {
            return 3;
        }

        @Override
        public Item brew(java.util.ArrayList<Item> ingredients) {
            if (!testIngredients(ingredients)) return null;

            int neededRunestones = 2;
            int neededSeeds = 1;

            for (Item item : ingredients) {
                if (item instanceof Runestone && neededRunestones > 0) {
                    int consume = Math.min(item.quantity(), neededRunestones);
                    item.quantity(item.quantity() - consume);
                    neededRunestones -= consume;
                } else if (item instanceof Plant.Seed && neededSeeds > 0) { // 🌟 Plant.Seed 로 변경
                    int consume = Math.min(item.quantity(), neededSeeds);
                    item.quantity(item.quantity() - consume);
                    neededSeeds -= consume;
                }
            }

            return sampleOutput(ingredients);
        }

        @Override
        public Item sampleOutput(java.util.ArrayList<Item> ingredients) {
            SpiderMineItem mine = new SpiderMineItem();
            mine.quantity(3);
            mine.identify();
            return mine;
        }
    }
}