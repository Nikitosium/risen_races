package hik1tka.risen_races.mixin;

import hik1tka.risen_races.register.ModEffect;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class MutPlayMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void checkPlayerZombification(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player.getWorld().isClient()) return;

        StatusEffectInstance effect = player.getStatusEffect(ModEffect.ZOMBIFICATION);
        if (effect == null || effect.getDuration() > 1) return;

        ServerWorld world = (ServerWorld) player.getWorld();
        // Знімаємо ефект ПЕРШИМ ділом - інакше цей блок спрацює ще раз на наступному тіку
        player.removeStatusEffect(ModEffect.ZOMBIFICATION);

        world.getServer().getPlayerManager().broadcast(
                Text.literal(getRandomDeathMessage(player.getName().getString())), false);

        ZombieEntity zombie = EntityType.ZOMBIE.create(world);
        if (zombie != null) {
            zombie.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
            zombie.setCustomName(player.getName());
            zombie.setCustomNameVisible(true);

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack playerItem = player.getEquippedStack(slot);
                if (!playerItem.isEmpty()) {
                    zombie.equipStack(slot, playerItem.copy());
                    zombie.setEquipmentDropChance(slot, 0.0f);
                }
            }
            world.spawnEntity(zombie);
        }

        player.kill();
    }

    // TODO: два з чотирьох варіантів згадують реальні релігії/ідеології як
    // жарт - якщо плануєш публікувати мод, раджу замінити на нейтральні.
    private String getRandomDeathMessage(String name) {
        String[] messages = {
                name + " перетворився в зомбі",
                name + " був проклятий богами",
                name + " прийняв російську ідеологію",
                name + " прийняв іслам"
        };
        return messages[Random.create().nextInt(messages.length)];
    }
}
