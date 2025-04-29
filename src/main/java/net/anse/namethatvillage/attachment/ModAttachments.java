package net.anse.namethatvillage.attachment;

import net.anse.namethatvillage.NameThatVillage;
import net.anse.namethatvillage.data.VillageBellChunkData;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, NameThatVillage.MOD_ID);

    // Referencia directa al AttachmentType (sin RegistryObject)
    public static final AttachmentType<VillageBellChunkData> VILLAGE_BELL_CHUNK =
            AttachmentType.builder(VillageBellChunkData::new)
                    .serialize(VillageBellChunkData.CODEC, data -> true)
                    .build();

    static {
        ATTACHMENT_TYPES.register("village_bell_chunk", () -> VILLAGE_BELL_CHUNK);
    }
}
