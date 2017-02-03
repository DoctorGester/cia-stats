function getLang() {
    if (navigator.languages != undefined)
        return navigator.languages[0] || "";
    else
        return navigator.language || "";
}

var model = [];

var app = angular.module('app', [ "ngSanitize", "ngRoute", "highcharts-ng" ]);

app.config(function($routeProvider, $locationProvider) {
    $routeProvider.when("/heroes/:hero", {
        templateUrl: "/templates/hero.html",
        controller: "HeroDetailsCtrl"
    });

    $routeProvider.when("/heroes", {
        templateUrl: "/templates/heroList.html",
        controller: "HeroListCtrl"
    });

    $locationProvider.html5Mode(true);
});

app.controller("HeroListCtrl", function($scope, $http) {
    $http.get("/api/heroes/list", { async: true })
        .success(function (response) {
            $scope.heroes = response;
        });
});

app.controller("HeroDetailsCtrl", function ($scope, $http, $filter, $routeParams) {
    $scope.heroName = $routeParams.hero;
    $scope.language = getLang();

    if ($scope.language !== "ru-RU") {
        $scope.language = "en-US";
    }

    $http.get("/api/heroes/details/" + $scope.heroName, { async: true })
        .success(function(response) {
            $scope.hero = response;
        });

    $scope.selectLanguage = function (language) {
        $scope.language = language;
    };

    $scope.getAbilityImage = function (ability) {
        return "/images/abilities/%s".replace(/%s/, ability.texture);
    };

    $scope.getAbilityDescription = function (ability) {
        var token = ability.description[$scope.language];

        if (!token) {
            return "";
        }

        return token.replace(/\\"/g, "\"").replace(/\\\\\\\\/g, "");
    };

    $scope.isAttackAbility = function (ability) {
        return ability.name.endsWith("_a");
    };

    $scope.not = function (func) {
        return function (item) {
            return !func(item);
        }
    };

    $scope.winrateConfig = {
        loading: true,
        options: {
            chart: { type: "line" },
            tooltip: {
                formatter: function () {
                    return this.y.toFixed(1) + ' % across ' + this.point.games + ' games';
                }
            },
            legend: { enabled: false }
        },
        title: { text: "Winrate" },
        xAxis: {
            type: 'datetime',
            title: { text: 'Date' }
        },
        yAxis: { min: 25, max: 75 }
    };

    var loadHeroWinRates = function (hero) {
        $scope.winrateConfig.loading = true;

        $http.get("/stats/" + hero, { async: true })
            .success(function (response) {
                var seriesResult = [];
                var perDay = response.heroWinRatePerDate;

                angular.forEach(perDay, function (value, key) {
                    seriesResult.push({x: new Date(key), y: value.winRate * 100, games: value.games})
                }, seriesResult);

                seriesResult = $filter('orderBy')(seriesResult, "'x'");

                $scope.winrateConfig.series = [{data: seriesResult}];
                $scope.winrateConfig.loading = false;
                $scope.winratePerPlayer = response.heroWinRatePerPlayer;

                angular.forEach($scope.winratePerPlayer, function (value) {
                    value.winRate = parseFloat(Math.round(value.winRateAndGames.winRate * 100)).toFixed(1);
                });
            });
    };

    loadHeroWinRates($scope.heroName);
});