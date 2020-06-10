package draylar.stophiding.key;

import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static FabricKeyBinding STOP_HIDING = FabricKeyBinding.Builder.create(
            new Identifier("stophiding", "key"),
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_T,
            "Stop Hiding").build();

    public static void init() {
        KeyBindingRegistry.INSTANCE.addCategory("Stop Hiding");
        KeyBindingRegistry.INSTANCE.register(STOP_HIDING);
    }
}
