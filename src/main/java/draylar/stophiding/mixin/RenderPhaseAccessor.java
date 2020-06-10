package draylar.stophiding.mixin;

import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderPhase.class)
public interface RenderPhaseAccessor {
    @Accessor
    static RenderPhase.Transparency getTRANSLUCENT_TRANSPARENCY() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static RenderPhase.DepthTest getALWAYS_DEPTH_TEST() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static RenderPhase.WriteMaskState getCOLOR_MASK() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static RenderPhase.Layering getPROJECTION_LAYERING() {
        throw new UnsupportedOperationException();
    }
}
