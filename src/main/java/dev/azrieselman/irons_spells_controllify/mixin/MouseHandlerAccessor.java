package dev.azrieselman.irons_spells_controllify.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {
    @Accessor("xpos")
    void ironsSpellsControllify$setXpos(double xpos);

    @Accessor("ypos")
    void ironsSpellsControllify$setYpos(double ypos);
}
