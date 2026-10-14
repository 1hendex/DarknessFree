package ac.darkness.darkness.platform.fabric.mixins;

import ac.darkness.darkness.GrimAPI;
import ac.darkness.darkness.platform.api.player.PlatformPlayer;
import ac.darkness.darkness.platform.api.sender.Sender;
import ac.darkness.darkness.platform.fabric.GrimACFabricIntermediaryLoaderPlugin;
import ac.darkness.darkness.platform.fabric.sender.FabricIntermediarySenderFactory;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

@Mixin(CommandSourceStack.class)
@Implements(@Interface(iface = Sender.class, prefix = "grim$"))
abstract class FabricIntermediaryCommandSourceStackMixin {

    @Unique
    private CommandSourceStack grimac$self() {
        return (CommandSourceStack) (Object) this;
    }

    @Unique
    private FabricIntermediarySenderFactory grimac$factory() {
        return GrimACFabricIntermediaryLoaderPlugin.LOADER.getFabricSenderFactory();
    }

    public UUID grim$getUniqueId() {
        return grimac$factory().getUniqueId(grimac$self());
    }

    public String grim$getName() {
        return grimac$factory().getName(grimac$self());
    }

    public void grim$sendMessage(String message) {
        grimac$factory().sendMessage(grimac$self(), message);
    }

    public void grim$sendMessage(Component message) {
        grimac$factory().sendMessage(grimac$self(), message);
    }

    public boolean grim$hasPermission(String permission) {
        return grimac$factory().hasPermission(grimac$self(), permission);
    }

    public boolean grim$hasPermission(String permission, boolean defaultIfUnset) {
        return grimac$factory().hasPermission(grimac$self(), permission, defaultIfUnset);
    }

    public void grim$performCommand(String commandLine) {
        grimac$factory().performCommand(grimac$self(), commandLine);
    }

    public boolean grim$isConsole() {
        return grimac$factory().isConsole(grimac$self());
    }

    public Object grim$getNativeSender() {
        return grimac$self();
    }

    public @Nullable PlatformPlayer grim$getPlatformPlayer() {
        return GrimAPI.INSTANCE.getPlatformPlayerFactory().getFromUUID(grim$getUniqueId());
    }
}
