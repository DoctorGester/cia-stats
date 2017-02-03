package com.dglab.cia;

import com.dglab.cia.data.*;
import com.dglab.cia.json.util.KvUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by shoujo on 2/2/2017.
 */
@Service
public class DataFetcherService {
    private GithubHelper helper = new GithubHelper();
    private static final Pattern HERO_INCLUDE_PATTERN = Pattern.compile("#base[\\s\t]+\".*\\\\(.*)\\.txt\"");

    private static Map<String, String> languageFiles = new HashMap<>();

    static {
        languageFiles.put("en-US", "game/panorama/localization/addon_english.txt");
        languageFiles.put("ru-RU", "game/panorama/localization/addon_russian.txt");
    }

    private RemoteAsyncExpiringObject<List<String>> heroList = new RemoteAsyncExpiringObject<>(
            helper,
            "game/scripts/npc/npc_heroes_custom.txt",
            this::parseHeroList
    );

    private Map<String, RemoteAsyncExpiringObject<KeyValueLocalization>> localization = new HashMap<>();
    private Map<String, RemoteAsyncExpiringObject<Hero>> heroMap = new HashMap<>();
    private Map<String, RemoteAsyncExpiringObject<KeyValueAbility>> abilityMap = new HashMap<>();

    private KeyValueLocalization getLocalizationForLanguage(String language) {
        return localization.computeIfAbsent(language, l -> new RemoteAsyncExpiringObject<>(
                helper,
                languageFiles.get(language),
                (data) -> KvUtil.parseKV(data, KeyValueLocalization.class, true)
        )).get();
    }

    private List<String> parseHeroList(String data) {
        return Stream.of(data.split("\\r?\\n"))
                .map(HERO_INCLUDE_PATTERN::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group(1))
                .collect(Collectors.toList());
    }

    private void localize(BiConsumer<String, String> consumer, String token) {
        for (String language : languageFiles.keySet()) {
            KeyValueLocalization localization = getLocalizationForLanguage(language);

            consumer.accept(language, localization.getString(token));
        }
    }

    private Hero parseHero(String hero, String data) {
        KeyValueHero kvHero = KvUtil.parseKV(data, KeyValueHero.class, true);
        Hero createdHero = new Hero();
        localize(createdHero::putName, "HeroName_npc_dota_hero_" + hero);

        // Type is important
        LinkedHashMap<String, HeroAbility> abilitiesByName = new LinkedHashMap<>();

        for (final String ability : kvHero.getAbilities()) {
            if (StringUtils.isEmpty(ability) || ability.startsWith("placeholder")) {
                continue;
            }

            KeyValueAbility kvAbility = abilityMap.computeIfAbsent(ability, (a) ->
                    new RemoteAsyncExpiringObject<>(
                            helper,
                            "game/scripts/npc/abilities/" + hero + "/" + ability + ".txt",
                            abilityData -> KvUtil.parseKV(abilityData, KeyValueAbility.class, true)
                    )
            ).get();

            String customIcon = kvHero.getCustomIcons().get(ability);

            HeroAbility createdAbility = new HeroAbility();
            createdAbility.setName(ability);
            createdAbility.setTexture(customIcon == null ? kvAbility.getTexture() + "_png.png" : "custom/" + customIcon);
            createdAbility.setCooldown(kvAbility.getCooldown());
            createdAbility.setDamage(kvAbility.getDamage());

            localize(createdAbility::putDescription, "AbilityTooltip_" + ability);

            if (ability.endsWith("_sub")) {
                HeroAbility parent = abilitiesByName.get(ability.substring(0, ability.length() - "_sub".length()));
                parent.setSub(createdAbility);
            } else {
                abilitiesByName.put(ability, createdAbility);
            }
        }

        createdHero.setAbilities(new ArrayList<>(abilitiesByName.values()));

        return createdHero;
    }

    public List<String> getHeroList() {
        return heroList.get();
    }

    public Hero getHero(String hero) {
        return heroMap.computeIfAbsent(hero, (h) ->
            new RemoteAsyncExpiringObject<>(
                    helper,
                    "game/scripts/npc/heroes/" + hero + ".txt",
                    (data) -> parseHero(hero, data)
            )
        ).get();
    }
}
