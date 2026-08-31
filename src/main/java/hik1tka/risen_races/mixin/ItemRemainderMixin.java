package hik1tka.risen_races.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemRemainderMixin {
    @Inject(method = "getRecipeRemainder", at = @At("HEAD"), cancellable = true)
    private void onGetRecipeRemainder(CallbackInfoReturnable<Item> cir) {
        Item item = (Item) (Object) this;

        if (item == Items.EXPERIENCE_BOTTLE) {
            cir.setReturnValue(Items.GLASS_BOTTLE);
        }
        // TODO: у оригіналі тут була гілка "AMETHYST_SHARD -> AMETHYST_SHARD" -
        // повертає той самий предмет як "залишок", що по факту нічого не робить
        // (no-op). Схоже на недописану заготовку під рецепт зілля - приберав,
        // скажи, що там мало повертатись, якщо ця гілка була потрібна.
    }
}
