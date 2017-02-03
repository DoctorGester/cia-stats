package com.dglab.cia;

import com.dglab.cia.data.Hero;
import com.dglab.cia.data.HeroAbility;
import com.dglab.cia.data.KeyValueAbility;
import com.dglab.cia.data.KeyValueHero;
import com.dglab.cia.json.util.KvUtil;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
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

    private RemoteAsyncExpiringObject<List<String>> heroList = new RemoteAsyncExpiringObject<>(
            helper,
            "game/scripts/npc/npc_heroes_custom.txt",
            this::parseHeroList
    );

    private RemoteAsyncExpiringObject<Object> localization = new RemoteAsyncExpiringObject<>(
            helper,
            "game/panorama/localization/addon_english.txt",
            (data) -> KvUtil.parseKV(data).get("addon")
    );

    private Map<String, RemoteAsyncExpiringObject<Hero>> heroMap = new HashMap<>();
    private Map<String, RemoteAsyncExpiringObject<KeyValueAbility>> abilityMap = new HashMap<>();

    @PostConstruct
    public void init() {
        KeyValueHero keyValueHero = new RemoteAsyncExpiringObject<>(
                helper,
                "game/scripts/npc/heroes/zuus.txt",
                (data) -> KvUtil.parseKV(data, KeyValueHero.class, true)
        ).get();

        System.out.println(keyValueHero);
    }

    private List<String> parseHeroList(String data) {
        return Stream.of(data.split("\\r?\\n"))
                .map(HERO_INCLUDE_PATTERN::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group(1))
                .collect(Collectors.toList());
    }

    private Hero parseHero(String hero, String data) {
        KeyValueHero kvHero = KvUtil.parseKV(data, KeyValueHero.class, true);
        Hero createdHero = new Hero();

        // Type is important
        LinkedHashMap<String, HeroAbility> abilitiesByName = new LinkedHashMap<>();

        for (final String ability : kvHero.getAbilities()) {
            if (ability.startsWith("placeholder")) {
                continue;
            }

            KeyValueAbility kvAbility = abilityMap.computeIfAbsent(ability, (a) ->
                    new RemoteAsyncExpiringObject<>(
                            helper,
                            "game/scripts/npc/abilities/" + hero + "/" + ability + ".txt",
                            abilityData -> KvUtil.parseKV(abilityData, KeyValueAbility.class, true)
                    )
            ).get();

            HeroAbility createdAbility = new HeroAbility();
            createdAbility.setName(ability);
            createdAbility.setTexture(kvAbility.getTexture());
            createdAbility.setCooldown(kvAbility.getCooldown());
            createdAbility.setDamage(kvAbility.getDamage());

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

    public Object getLocalization() {
        return localization.get();
    }
}
