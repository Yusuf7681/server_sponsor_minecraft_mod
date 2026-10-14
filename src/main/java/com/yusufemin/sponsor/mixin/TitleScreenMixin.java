package com.yusufemin.sponsor.mixin;

import com.yusufemin.sponsor.SponsorConfig;
import com.yusufemin.sponsor.SponsorPopupScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void server_sponsor$addSponsorButton(CallbackInfo ci) {
        SponsorConfig cfg = SponsorConfig.get();

        Button multiplayer = null;
        for (GuiEventListener child : this.children()) {
            if (child instanceof Button b
                    && b.getMessage().getContents() instanceof TranslatableContents tc
                    && tc.getKey().equals("menu.multiplayer")) {
                multiplayer = b;
                break;
            }
        }

        int x;
        int y;
        int h;
        if (multiplayer != null) {
            x = multiplayer.getX() + multiplayer.getWidth() + 4;
            y = multiplayer.getY();
            h = multiplayer.getHeight();
        } else {
            x = this.width / 2 - 100 + 200 + 4;
            y = this.height / 4 + 48 + 24;
            h = 20;
        }

        this.addRenderableWidget(
                Button.builder(Component.literal(cfg.buttonText).withStyle(ChatFormatting.GREEN),
                                button -> this.minecraft.setScreen(new SponsorPopupScreen(this)))
                        .bounds(x + cfg.xOffset, y + cfg.yOffset, cfg.buttonWidth, h)
                        .build()
        );
    }
}
