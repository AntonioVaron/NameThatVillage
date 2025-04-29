package net.anse.namethatvillage.data;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;

public class VillageBellChunkData{
    private boolean replaced = false;


    public static final Codec<VillageBellChunkData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("replaced").forGetter(data -> data.replaced)
    ).apply(instance, replaced -> {
        VillageBellChunkData data = new VillageBellChunkData();
        data.replaced = replaced;
        return data;
    }));


    public boolean isReplaced() {
        return replaced;
    }

    public void markReplaced() {
        this.replaced = true;
    }

    public void load(CompoundTag tag) {
        this.replaced = tag.getBoolean("replaced");
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("replaced", this.replaced);
        return tag;
    }
}
