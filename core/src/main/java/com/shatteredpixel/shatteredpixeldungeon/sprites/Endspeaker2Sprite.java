package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

public class Endspeaker2Sprite extends MobSprite {

    public Endspeaker2Sprite() {
        super();

        texture( Assets.Sprites.ENDSPEAKER2 );

        TextureFilm frames = new TextureFilm( texture, 44, 28 );

        idle = new Animation( 2, true );
        idle.frames( frames, 0, 0, 0, 0 );

        run = new Animation( 15, true );
        run.frames( frames, 0 );

        attack = new Animation( 12, false );
        attack.frames( frames, 0 );

        die = new Animation( 5, false );
        die.frames( frames, 0 );

        play( idle );
    }

}
