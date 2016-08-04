package com.dglab.cia.database;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author doc
 */
@Embeddable
public class HeroWinRateKey extends MatchKey {
    private String heroName;

    @Column(name = "hero", nullable = false, updatable = false)
    public String getHeroName() {
        return heroName;
    }

    public void setHeroName(String heroName) {
        this.heroName = heroName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        HeroWinRateKey that = (HeroWinRateKey) o;

        return heroName != null ? heroName.equals(that.heroName) : that.heroName == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (heroName != null ? heroName.hashCode() : 0);
        return result;
    }
}
