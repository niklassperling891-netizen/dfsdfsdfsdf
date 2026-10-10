package de.example.dropmatching;

import de.example.dropmatching.mixin.HandledScreenAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class DropMatchingClient implements ClientModInitializer {
    private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(
            net.minecraft.util.Identifier.of("drop_matching_items", "general")
    );

    @Override
    public void onInitializeClient() {
        KeyBinding dropKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.drop_matching_items.drop_matching",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (dropKey.wasPressed()) {
                if (client.player == null || client.interactionManager == null
                        || !(client.currentScreen instanceof HandledScreen<?> screen)) {
                    continue;
                }

                Slot hovered = ((HandledScreenAccessor) screen).dropMatching$getFocusedSlot();
                if (hovered == null || !hovered.hasStack()) {
                    continue;
                }

                ItemStack template = hovered.getStack().copy();
                ScreenHandler handler = screen.getScreenHandler();
                List<Integer> matchingSlots = new ArrayList<>();

                for (Slot slot : handler.slots) {
                    if (slot.hasStack() && ItemStack.areItemsEqual(template, slot.getStack())) {
                        matchingSlots.add(slot.id);
                    }
                }

                for (int slotId : matchingSlots) {
                    client.interactionManager.clickSlot(
                            handler.syncId,
                            slotId,
                            1,
                            SlotActionType.THROW,
                            client.player
                    );
                }
            }
        });
    }
}
