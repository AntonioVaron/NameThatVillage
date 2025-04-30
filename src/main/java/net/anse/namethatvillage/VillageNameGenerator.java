package net.anse.namethatvillage;

import java.util.*;

public class VillageNameGenerator {

    private static final Random random = new Random();

    // Mapa para almacenar los nombres por tipo de aldea (usamos String en lugar de Structure)
    private static final Map<String, List<String>> villageNamesByType = new HashMap<>();

    static {
        // Inicializamos los nombres para cada tipo de aldea
        initializeVillageNames();
    }

    private static void initializeVillageNames() {
        // Aldeas nevadas
        villageNamesByType.put("snowy", Arrays.asList(
                "Frostbite", "Winterholm", "Skjorn", "Lirwen", "Rhovan", "Kaedwind",
                "Dornak", "Grendor", "Northgate", "Froden", "Valdrikhelm", "Blancara",
                "Nivalis", "Kristaval", "Venther", "Eldoria", "Coldfront", "Astherya",
                "Shiverstone", "Icespire"
        ));

        // Aldeas desérticas
        villageNamesByType.put("desert", Arrays.asList(
                "Shambaia", "Sandspring", "Dusthaven", "Oasis Reach", "Arakar", "Zalhira",
                "Thareq", "Iztamel", "Oksara", "Vaelmir", "Khasim", "Erezkar", "Xhantil",
                "Jorvan", "Yzahel", "Mazzir", "Tzaref", "Kovarek", "Scorchpoint", "Al’Namir"
        ));

        // Aldeas de jungla
        villageNamesByType.put("jungle", Arrays.asList(
                "Yaviru", "Leafshade", "Xolanta", "Zahanì", "Aztika", "Nakuara", "Quilora",
                "Ombara", "Ukambi", "Wildroot", "Zaimiri", "Takora", "Jhamilu", "Sombora",
                "Keltari", "Uhlame", "Quilanga", "Tumakai", "Selvora", "Tropikana"
        ));

        // Aldeas de sabana
        villageNamesByType.put("savanna", Arrays.asList(
                "Amsari", "Tarouk", "Savira", "Taimari", "Kahalari", "Sunhaven", "Veldt",
                "Kembara", "Bahren", "Solstone", "Baohim", "Yambar", "Drydertar", "Marbaku",
                "Sahanke", "Belokar", "Akhira", "Zimbari", "Kotalu", "Sunreach"
        ));

        // Aldeas de pantano
        villageNamesByType.put("swamp", Arrays.asList(
                "Murgash", "Mirewood", "Bogreach", "Fennmar", "Bogmire", "Mudlurk",
                "Darkwater", "Marshren", "Zulthang", "Blormn", "Molkuid", "Murkhaz",
                "Duskkerg", "Rotheris", "Xhulnor", "Holkua", "Zcyerghelg", "Thulven",
                "Shargull", "Narswamp"
        ));
        // Aldeas de llanura
        villageNamesByType.put("plains", Arrays.asList(
                "Grangefield", "Greenholt", "Breezewind", "Ventoria", "Meadowbrook",
                "Myrule", "Bristlands", "Stanemoor", "Altamoral", "New Hespire", "Estebral",
                "Fresneld", "Valderimnd", "Florameld", "Aurivento", "Cezara", "Norbury", "Haverstead",
                "Silvergreens", "Cresthome"
        ));

        // Aldeas de bosque
        villageNamesByType.put("forest", Arrays.asList(
                "Woodshire", "Brambleshold", "Oakmeads", "Erenpoint", "Fernleigh",
                "Treefall", "Timberven", "Orvian", "Noss", "Howernev", "Sylmedril",
                "Velsion", "Nytharast Hollow", "Hespire", "Backtrees Backrack", "Verdeval",
                "Tarven", "Copa Clarea", "Gaderile", "Fall Crust"
        ));

        // Aldeas de colinas
        villageNamesByType.put("hills", Arrays.asList(
                "Hana Hills", "Pinecrest", "Humpstar", "Grudel Valley", "Greenway",
                "Cliffhaven", "Tillbent", "Auren", "Harenood", "Ygverville", "Karkalemb Village",
                "Xartad", "Zquivermend", "Tugarmor", "Long Line Lost", "Bumper Plains", "Jerbind",
                "Laggald Bengald", "Werv"
        ));
    }

    public static String generateVillageName(String biomePath) {
        biomePath = biomePath.toLowerCase();

        if (biomePath.contains("snow") || biomePath.contains("frozen") || biomePath.contains("cold")) {
            return getRandomName(villageNamesByType.get("snowy"));
        } else if (biomePath.contains("desert")) {
            return getRandomName(villageNamesByType.get("desert"));
        } else if (biomePath.contains("jungle")) {
            return getRandomName(villageNamesByType.get("jungle"));
        } else if (biomePath.contains("savanna") || biomePath.contains("savana")) {
            return getRandomName(villageNamesByType.get("savanna"));
        } else if (biomePath.contains("swamp") || biomePath.contains("mangrove") || biomePath.contains("marsh")) {
            return getRandomName(villageNamesByType.get("swamp"));
        } else if (biomePath.contains("plains") || biomePath.contains("meadow")) {
            return getRandomName(villageNamesByType.get("plains"));
        } else if (biomePath.contains("forest") || biomePath.contains("taiga")) {
            return getRandomName(villageNamesByType.get("forest"));
        } else if (biomePath.contains("hills")) {
            return getRandomName(villageNamesByType.get("hills"));
        }  else {
            // Recoger todos los nombres de todas las listas
            List<String> allNames = villageNamesByType.values().stream()
                    .flatMap(List::stream)
                    .toList();
            return getRandomName(allNames);
        }
    }
    private static String getRandomName(List<String> namesList) {
        return namesList.get(random.nextInt(namesList.size()));
    }
}
