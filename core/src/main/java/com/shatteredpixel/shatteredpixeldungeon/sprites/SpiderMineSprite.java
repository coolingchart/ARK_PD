package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

public class SpiderMineSprite extends MobSprite {

    public SpiderMineSprite() {
        super();

        // 1. Assets에 등록해 둔 지뢰 NPC 이미지 파일 경로를 연결
        texture( Assets.Sprites.SPIDERMINE );

        // 2. 가로세로 픽셀 크기 지정 (예: 16x16)
        TextureFilm frames = new TextureFilm(texture, 16, 16);

        // 3. 대기, 걷기, 죽음 상태일 때 모두 0번째(첫 번째) 프레임 하나만 반복하도록 설정
        idle = new Animation(1, true);
        idle.frames(frames, 0);

        run = new Animation(1, true);
        run.frames(frames, 0);

        die = new Animation(1, false);
        die.frames(frames, 0);

        play(idle);
    }
}