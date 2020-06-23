package draylar.stophiding.key;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class KeyBindings {

    public static KeyBinding STOP_HIDING = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "stophiding:key",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_T,
                    "Stop Hiding")
            );


    public static void init() {

    }
}
