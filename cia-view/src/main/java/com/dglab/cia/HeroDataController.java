package com.dglab.cia;

import com.dglab.cia.data.Hero;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by shoujo on 2/2/2017.
 */
@RestController
@RequestMapping("/api/heroes")
public class HeroDataController {
    @Autowired
    private DataFetcherService dataFetcher;

    @GetMapping("/list")
    public List<String> getHeroList() {
        return dataFetcher.getHeroList();
    }

    @GetMapping("/details/{hero}")
    public Hero getHero(@PathVariable("hero") String hero) {
        return dataFetcher.getHero(hero);
    }
}
