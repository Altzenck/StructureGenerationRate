package dev.dexuby.structuregenerationrate.branch.v1_21_R1;

import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

public class NMSUtils {
    /*
    @SuppressWarnings({"deprecation", "rawtypes"})
    public static <T> Holder<T> registerOrOverride(MappedRegistry<T> mappedRegistry, ResourceKey<T> resourceKey, T value, RegistrationInfo registrationInfo) {
        Class<MappedRegistry> c = MappedRegistry.class;
        ReflectionUtils.callMethod(c, mappedRegistry, "h", ReflectionUtils.param(ResourceKey.class, resourceKey)); // validateWrite(ResourceKey)
        Objects.requireNonNull(resourceKey);
        Objects.requireNonNull(value);
        final ReflectionUtils.FieldReference<ObjectList<Holder.Reference<T>>> byIdRef = ReflectionUtils.getDeclaredField(c, mappedRegistry, "c"); // byId
        final ReflectionUtils.FieldReference<Map<ResourceLocation, Holder.Reference<T>>> byLocationRef = ReflectionUtils.getDeclaredField(c, mappedRegistry, "e"); // byLocation
        final ReflectionUtils.FieldReference<Map<ResourceKey<T>, Holder.Reference<T>>> byKeyRef = ReflectionUtils.getDeclaredField(c, mappedRegistry, "f"); // byKey
        final ReflectionUtils.FieldReference<Reference2IntMap<T>> toIdRef = ReflectionUtils.getDeclaredField(c, mappedRegistry, "d"); // toId
        final ReflectionUtils.FieldReference<Map<ResourceKey<T>, RegistrationInfo>> registrationInfosRef = ReflectionUtils.getDeclaredField(c, mappedRegistry, "h"); // registrationInfos
        final ReflectionUtils.FieldReference<Map<T, Holder.Reference<T>>> byValueRef = ReflectionUtils.getDeclaredField(c, mappedRegistry, "g"); // byValue
        final ReflectionUtils.FieldReference<Map<TagKey<T>, HolderSet.Named<T>>> frozenTagsRef = ReflectionUtils.getDeclaredField(c, mappedRegistry, "j"); // frozenTags
        final ReflectionUtils.FieldReference<Object> allTagsRef = ReflectionUtils.getDeclaredField(c, mappedRegistry, "k"); // allTags
        Holder<T> ref = byLocationRef.get().get(resourceKey.location());
        T val = (ref != null && ref.isBound())? ref.value() : null;
        if (val != null) {
            registrationInfosRef.get().remove(resourceKey);
            byIdRef.get().remove(toIdRef.get().getInt(val));
            toIdRef.get().remove(val);
            byLocationRef.get().remove(resourceKey.location());
            byKeyRef.get().remove(resourceKey);
            byValueRef.get().remove(val);
        }
        return mappedRegistry.register(resourceKey, value, registrationInfo);
    }
    */

    public static RandomSpreadType RandomSpreadTypeByName(String name) {
        try {
            return RandomSpreadType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
