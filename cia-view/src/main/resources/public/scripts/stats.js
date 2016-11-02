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

function createWinRateGraph(graphId, graphName, data) {
    var markers = getMarkersFromData(data, 1);
    var games = _.reduce(markers, function(a, b) { return a + b[2]; }, 0);
    var template = "http://cdn.dota2.com/apps/dota2/images/heroes/$_icon.png";

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
                y: 26
            }
        }
    };

    Highcharts.chart(graphId, {
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
            max: 100
        },
        tooltip: {
            formatter: function() {
                return '<b>' + this.point.name + '</b>: ' + this.y.toFixed(1) + ' %';
            }
        },
        title: {
            text: graphName
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