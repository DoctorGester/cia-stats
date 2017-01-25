function getMarkersFromData(data, sortIndex) {
    data = data.slice(0);
    data = _.map(data, function(point) { return point.slice(0); });

    var markers = _.sortBy(data, function (point) {
        return point[sortIndex];
    });

    _.each(markers, function(point) {
        point[0] = point[0].substring("npc_dota_hero_".length);
        point[1] *= 100;
    });

    return markers;
}

function createWinRateGraph(graphId, data) {
    var markers = getMarkersFromData(data, 1);
    var games = _.reduce(markers, function(a, b) { return a + b[2]; }, 0);
    var template = "http://cdn.dota2.com/apps/dota2/images/heroes/$_icon.png";
    var gamesMap = {};

    markers.forEach(function(m) {
        gamesMap[m[0]] = m[2];
    });

    var heroIconFormatter = function () {
        return '<img style="width: 24px; height: 24px;" src="' + template.replace("\$", this.point.name) + '">';
    };

    var heroIconPlotOptions = {
        column: {
            colorByPoint: true,
            dataLabels: {
                enabled: true,
                useHTML: true,
                formatter: heroIconFormatter,
                y: 40
            }
        }
    };

    return Highcharts.chart(graphId, {
        chart: {
            type: "column"
        },
        credits: {
            enabled: false
        },
        xAxis: {
            labels: {
                enabled: false
            }
        },
        yAxis: {
            min: 0,
            max: 100,
            tickInterval: 10
        },
        tooltip: {
            formatter: function() {
                return '<b>' + this.point.name + '</b>: ' + this.y.toFixed(1) + ' %, ' + gamesMap[this.key] + ' rounds';
            }
        },
        title: {
            text: ""
        },
        subtitle: { text: "based on " + games + " rounds"},
        plotOptions: heroIconPlotOptions,
        series: [{
            showInLegend: false,
            data: markers,
            cursor: 'pointer',
            point: {
                events: {
                    click: function() {
                        window.location.href = "/heroes/" + this.name;
                    }
                }
            }
        }]
    });

}

window.tabs = function(options) {
    var el = document.querySelector(options.el);
    var tabNavigationLinks = el.querySelectorAll(options.tabNavigationLinks);
    var tabContentContainers = el.querySelectorAll(options.tabContentContainers);
    var charts = options.charts;
    var activeIndex = 0;
    var initCalled = false;

    var init = function () {
        if (!initCalled) {
            initCalled = true;
            el.classList.remove('no-js');

            for (var i = 0; i < tabNavigationLinks.length; i++) {
                var link = tabNavigationLinks[i];
                handleClick(link, i);
            }
        }
    };

    var handleClick = function (link, index) {
        link.addEventListener('click', function (e) {
            e.preventDefault();
            goToTab(index);
        });
    };

    var goToTab = function (index) {
        if (index !== activeIndex && index >= 0 && index <= tabNavigationLinks.length) {
            tabNavigationLinks[activeIndex].classList.remove('is-active');
            tabNavigationLinks[index].classList.add('is-active');
            tabContentContainers[activeIndex].classList.remove('is-active');
            tabContentContainers[index].classList.add('is-active');
            activeIndex = index;
            charts[index].reflow();
        }
    };

    return {
        init: init,
        goToTab: goToTab
    };
};