
var chart_scoreCalculationSpeedProblemStatisticChart_a90f6 = new Chart(document.getElementById('chart_scoreCalculationSpeedProblemStatisticChart_a90f6'), {
    type: 'line',
    data: {
        datasets: [
            {
                  label: 'Hill Climbing',
                    borderWidth: 1
                  ,
                  data: [
                    {x: 1029, y: 32514}, {x: 2018, y: 19801}, {x: 3001, y: 14942}, {x: 4047, y: 12481}, {x: 5064, y: 10430}, {x: 6089, y: 7960}, {x: 7030, y: 7804}, {x: 8000, y: 7571}, {x: 9134, y: 6476}, {x: 10000, y: 5653}, {x: 11056, y: 5409}, {x: 12077, y: 5594}, {x: 13001, y: 5298}, {x: 14172, y: 4877}, {x: 15098, y: 4406}, {x: 16057, y: 4254}, {x: 17026, y: 4210}, {x: 18143, y: 4383}, {x: 19103, y: 4250}, {x: 20098, y: 4100}, {x: 21177, y: 3781}, {x: 22136, y: 3403}, {x: 23116, y: 3330}, {x: 24128, y: 3225}, {x: 25150, y: 3193}, {x: 26152, y: 3257}, {x: 27144, y: 3290}, {x: 28158, y: 3218}, {x: 29204, y: 3120}, {x: 30092, y: 2756}, {x: 31157, y: 2298}, {x: 32242, y: 2256}, {x: 33334, y: 2241}, {x: 34059, y: 2251}, {x: 35165, y: 2213}, {x: 36290, y: 2176}, {x: 37018, y: 2241}, {x: 38117, y: 2227}, {x: 39235, y: 2189}, {x: 40406, y: 2090}, {x: 41175, y: 2122}, {x: 42341, y: 2099}, {x: 43113, y: 2113}, {x: 44396, y: 1908}, {x: 45246, y: 1920}, {x: 46135, y: 1835}, {x: 47023, y: 1837}, {x: 48345, y: 1851}, {x: 49224, y: 1856}, {x: 50109, y: 1844}, {x: 51034, y: 1764}, {x: 52390, y: 1805}, {x: 53256, y: 1884}, {x: 54157, y: 1811}, {x: 55037, y: 1854}, {x: 56364, y: 1844}, {x: 57266, y: 1809}, {x: 58172, y: 1801}, {x: 59137, y: 1691}
                  ]
                }, 
{
                  label: 'Tabu Search (favorite)',
                    borderWidth: 4
,
                  data: [
                    {x: 1016, y: 32930}, {x: 2041, y: 19902}, {x: 3046, y: 14614}, {x: 4030, y: 12439}, {x: 5056, y: 10339}, {x: 6076, y: 8000}, {x: 7018, y: 7796}, {x: 8088, y: 7626}, {x: 9109, y: 6393}, {x: 10096, y: 5787}, {x: 11121, y: 5572}, {x: 12146, y: 5572}, {x: 13074, y: 5275}, {x: 14077, y: 4881}, {x: 15005, y: 4396}, {x: 16158, y: 4246}, {x: 17157, y: 4084}, {x: 18106, y: 4299}, {x: 19135, y: 3965}, {x: 20125, y: 4121}, {x: 21211, y: 3756}, {x: 22180, y: 3368}, {x: 23189, y: 3234}, {x: 24197, y: 3238}, {x: 25233, y: 3150}, {x: 26017, y: 3122}, {x: 27058, y: 3135}, {x: 28086, y: 3175}, {x: 29134, y: 3114}, {x: 30312, y: 2770}, {x: 31049, y: 2214}, {x: 32123, y: 2279}, {x: 33206, y: 2260}, {x: 34333, y: 2172}, {x: 35089, y: 2158}, {x: 36233, y: 2139}, {x: 37304, y: 2285}, {x: 38025, y: 2263}, {x: 39131, y: 2213}, {x: 40252, y: 2183}, {x: 41045, y: 2058}, {x: 42222, y: 2079}, {x: 43380, y: 2113}, {x: 44251, y: 1873}, {x: 45184, y: 1749}, {x: 46165, y: 1663}, {x: 47130, y: 1691}, {x: 48067, y: 1741}, {x: 49414, y: 1817}, {x: 50336, y: 1770}, {x: 51275, y: 1738}, {x: 52157, y: 1850}, {x: 53023, y: 1884}, {x: 54345, y: 1851}, {x: 55234, y: 1835}, {x: 56169, y: 1745}, {x: 57128, y: 1701}, {x: 58111, y: 1660}, {x: 59074, y: 1694}
                  ]
                }, 
{
                  label: 'Late Acceptance',
                    borderWidth: 1
                  ,
                  data: [
                    {x: 1029, y: 32514}, {x: 2015, y: 19862}, {x: 3001, y: 14896}, {x: 4045, y: 12505}, {x: 5067, y: 10379}, {x: 6085, y: 8015}, {x: 7022, y: 7837}, {x: 8004, y: 7478}, {x: 9032, y: 6350}, {x: 10016, y: 5804}, {x: 11056, y: 5492}, {x: 12085, y: 5551}, {x: 13014, y: 5270}, {x: 14006, y: 4935}, {x: 15105, y: 4454}, {x: 16056, y: 4290}, {x: 17021, y: 4227}, {x: 18153, y: 4325}, {x: 19129, y: 4180}, {x: 20133, y: 4063}, {x: 21238, y: 3692}, {x: 22201, y: 3389}, {x: 23185, y: 3317}, {x: 24213, y: 3175}, {x: 25258, y: 3123}, {x: 26241, y: 3320}, {x: 27248, y: 3241}, {x: 28014, y: 3195}, {x: 29054, y: 3138}, {x: 30200, y: 2848}, {x: 31272, y: 2283}, {x: 32381, y: 2207}, {x: 33105, y: 2254}, {x: 34212, y: 2211}, {x: 35351, y: 2149}, {x: 36115, y: 2136}, {x: 37236, y: 2183}, {x: 38349, y: 2199}, {x: 39109, y: 2147}, {x: 40243, y: 2158}, {x: 41025, y: 2086}, {x: 42184, y: 2112}, {x: 43360, y: 2081}, {x: 44247, y: 1839}, {x: 45119, y: 1871}, {x: 46432, y: 1864}, {x: 47329, y: 1819}, {x: 48228, y: 1815}, {x: 49130, y: 1809}, {x: 50073, y: 1730}, {x: 51008, y: 1745}, {x: 52351, y: 1822}, {x: 53215, y: 1888}, {x: 54105, y: 1833}, {x: 55007, y: 1809}, {x: 56386, y: 1775}, {x: 57303, y: 1779}, {x: 58250, y: 1723}, {x: 59232, y: 1661}
                  ]
                }, 
{
                  label: 'Simulated Annealing',
                    borderWidth: 1
                  ,
                  data: [
                    {x: 1029, y: 32514}, {x: 2021, y: 19741}, {x: 3011, y: 14836}, {x: 4059, y: 12458}, {x: 5091, y: 10279}, {x: 6021, y: 7896}, {x: 7064, y: 7823}, {x: 8060, y: 7373}, {x: 9110, y: 6217}, {x: 10115, y: 5683}, {x: 11003, y: 5513}, {x: 12020, y: 5616}, {x: 13088, y: 5348}, {x: 14070, y: 4985}, {x: 15167, y: 4463}, {x: 16108, y: 4335}, {x: 17142, y: 3945}, {x: 18079, y: 4354}, {x: 19040, y: 4245}, {x: 20024, y: 4146}, {x: 21085, y: 3845}, {x: 22047, y: 3392}, {x: 23049, y: 3257}, {x: 24087, y: 3144}, {x: 25143, y: 3090}, {x: 26135, y: 3290}, {x: 27119, y: 3317}, {x: 28132, y: 3222}, {x: 29159, y: 3178}, {x: 30296, y: 2870}, {x: 31019, y: 2257}, {x: 32134, y: 2195}, {x: 33249, y: 2195}, {x: 34391, y: 2143}, {x: 35215, y: 1980}, {x: 36376, y: 2108}, {x: 37112, y: 2217}, {x: 38205, y: 2239}, {x: 39310, y: 2215}, {x: 40072, y: 2141}, {x: 41222, y: 2128}, {x: 42369, y: 2134}, {x: 43146, y: 2100}, {x: 44408, y: 1939}, {x: 45273, y: 1886}, {x: 46142, y: 1878}, {x: 47050, y: 1797}, {x: 48410, y: 1800}, {x: 49328, y: 1777}, {x: 50247, y: 1775}, {x: 51190, y: 1730}, {x: 52095, y: 1803}, {x: 53003, y: 1797}, {x: 54345, y: 1824}, {x: 55249, y: 1805}, {x: 56160, y: 1791}, {x: 57096, y: 1743}, {x: 58091, y: 1640}, {x: 59016, y: 1764}
                  ]
                }
        ]
    },
    options: {
        animation: false,
        responsive: true,
        maintainAspectRatio: false,
        spanGaps: true,
        plugins: {
            title: {
                display: true,
                text: 'Problem_0 score calculation speed statistic'
            },
            tooltip: {
                callbacks: {
                        title: function(context) {
                            return humanizeTime(context[0].parsed.x);
                        }
                        
                }
            }
        },
        scales: {
            x: {
                title: {
                    display: true,
                    text: 'Time spent'
                },
                ticks: {
                        stepSize: 1000
                        ,
                        callback: function(value, index) {
                            return humanizeTime(value);
                        }
                },
                suggestedMin: 0,
                suggestedMax: 59232,
                type: 'linear',
                display: true
            },
            y: {
                title: {
                    display: true,
                    text: 'Score calculation speed per second (logarithmic)'
                },
                ticks: {
                },
                type: 'logarithmic',
                display: true
            }
        },
watermark: {
    image: "website/img/timefold-logo-stacked-positive.svg",
    x: 15,
    y: 15,
    width: 48,
    height: 50,
    opacity: 0.1,
    alignX: "right",
    alignY: "bottom",
    alignToChartArea: true,
    position: "front",
}    },
plugins: [{ 
    id: 'customPlugin',
    beforeDraw: (chart, args, options) => {
          const ctx = chart.canvas.getContext('2d');
          ctx.save();
          ctx.globalCompositeOperation = 'destination-over';
          ctx.fillStyle = 'white';
          ctx.fillRect(0, 0, chart.canvas.width, chart.canvas.height);
          ctx.restore();
    }
}]
});

window.addEventListener('beforeprint', () => {
  chart_scoreCalculationSpeedProblemStatisticChart_a90f6.resize(1280, 720);
});
window.addEventListener('afterprint', () => {
  chart_scoreCalculationSpeedProblemStatisticChart_a90f6.resize();
});
