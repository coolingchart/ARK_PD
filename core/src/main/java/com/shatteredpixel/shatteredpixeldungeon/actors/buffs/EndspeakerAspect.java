package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.miniboss.TheEndspeaker;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;

public abstract class EndspeakerAspect extends Buff {

    {
        type = buffType.POSITIVE;
    }

    protected int color;
    protected EndspeakerAbility ability;
    protected float auraSpeed = 90f;

    @Override
    public int icon() {
        return BuffIndicator.CORRUPT;
    }

    @Override
    public void tintIcon(Image icon) {
        icon.hardlight(color);
    }

    @Override
    public void fx(boolean on) {
        if (on) target.sprite.aura( color, auraSpeed );
        else target.sprite.clearAura();
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc");
    }

    public boolean giveAbility() {
        if (this.ability != null) {
            TheEndspeaker.Status.activate(this.ability);
            return true;
        }
        return false;
    }

    public enum EndspeakerAbility {
        SPELL_ABSORPTION,
        INCREASED_RANGE,
        RAMP_UP,
        CHARGE,
        HARDENING,
        CROWD_CONTROL_IMMUNE,

    }

    // Depth 36 pair (teal/aquamarine — magic)
    public static class SpellAbsorption extends EndspeakerAspect {
        {
            color = 0xFF00CCBB; // teal — distinct from Giant blue (#0088FF) and AntiMagic green (#00FF00)
            auraSpeed = 90f;
            ability = EndspeakerAbility.SPELL_ABSORPTION;
        }
    }

    public static class IncreasedRange extends EndspeakerAspect {
        {
            color = 0xFF99FFEE; // aquamarine — lighter/more balanced than pure green
            auraSpeed = 45f;   // slow pulse — passive reach
            ability = EndspeakerAbility.INCREASED_RANGE;
        }
    }

    // Depth 37 pair (magenta/pink — aggression)
    public static class RampUp extends EndspeakerAspect {
        {
            color = 0xFFFF1188; // hot magenta — distinct from red (#FF0000) and orange (#FF8800)
            auraSpeed = 180f;  // fast spin — escalating fury
            ability = EndspeakerAbility.RAMP_UP;
        }
    }

    public static class Charge extends EndspeakerAspect {
        {
            color = 0xFFFF7799; // salmon pink — distinct from red and orange
            auraSpeed = 270f;  // very fast — kinetic charge
            ability = EndspeakerAbility.CHARGE;
        }
    }

    // Depth 38 pair (brown/amethyst — defense)
    public static class Hardening extends EndspeakerAspect {
        {
            color = 0xFF885533; // dark sienna — distinct from orange (#FF8800)
            auraSpeed = 60f;   // slow — heavy armor
            ability = EndspeakerAbility.HARDENING;
        }
    }

    public static class CrowdControlImmune extends EndspeakerAspect {
        {
            color = 0xFFBB88FF; // soft amethyst — much lighter than Projecting purple (#8800FF)
            auraSpeed = -90f;  // counter-clockwise — repelling force
            ability = EndspeakerAbility.CROWD_CONTROL_IMMUNE;
        }
    }

    public static class Empowering extends EndspeakerAspect {
        {
            color = 0xFFFFCCEE; // pale rose — distinct from Blessed yellow (#FFFF00) and all others
            auraSpeed = 150f;  // medium-fast — powered up
        }
    }
}
