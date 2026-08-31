package hik1tka.risen_races.mixin;

import hik1tka.risen_races.entity.humanoid.human.util.NPCConversionHandler;
import hik1tka.risen_races.register.ModEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Зберігає/читає MD_NPC_Memory (записану в NPCConversionHandler.humanToZombie)
 * і в момент закінчення PURIFICATION передає її в NPCConversionHandler.zombieToHuman,
 * який відновлює точні дані. Якщо пам'яті нема (дикий зомбі) - той самий метод
 * підхопить фолбек-гілку і згенерує нову людину.
 */
@Mixin(ZombieEntity.class)
public abstract class PurifHumMixin {

    @Unique
    private NbtCompound mdNpcMemory;

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomNpcData(NbtCompound nbt, CallbackInfo ci) {
        if (this.mdNpcMemory != null) {
            nbt.put("MD_NPC_Memory", this.mdNpcMemory);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomNpcData(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("MD_NPC_Memory")) {
            this.mdNpcMemory = nbt.getCompound("MD_NPC_Memory");
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void checkNPCPurification(CallbackInfo ci) {
        ZombieEntity zombie = (ZombieEntity) (Object) this;
        // ZombieVillagerEntity - підклас ZombieEntity, але його вже повністю
        // обробляє PurifVilMixin (лікує назад у VillagerEntity) - якщо не
        // виключити тут, спрацюють обидва міксіни одночасно і зомбі спробує
        // конвертуватись у два різні боки в один тік.
        if (zombie.getWorld().isClient() || zombie instanceof ZombieVillagerEntity) return;

        StatusEffectInstance effect = zombie.getStatusEffect(ModEffect.PURIFICATION);
        if (effect == null) return;

        // Пам'ять могла ще не потрапити в поле (наприклад, одразу після
        // конвертації в той самий тік) - підстраховуємось читанням з NBT.
        if (this.mdNpcMemory == null) {
            NbtCompound tempNbt = new NbtCompound();
            zombie.writeNbt(tempNbt);
            if (tempNbt.contains("MD_NPC_Memory")) {
                this.mdNpcMemory = tempNbt.getCompound("MD_NPC_Memory");
            }
        }

        if (effect.getDuration() <= 10) {
            NPCConversionHandler.zombieToHuman((ServerWorld) zombie.getWorld(), zombie, this.mdNpcMemory);
        }
    }
}
