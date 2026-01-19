
var chart_bestScoreDistributionSummaryChart0_dba40 = new Chart(document.getElementById('chart_bestScoreDistributionSummaryChart0_dba40'), {
    type: 'boxplot',
    data: {
        labels: [
            'Problem_0'
        ],
        datasets: [
                {
                    label: 'Hill Climbing',
                        borderWidth: 1
                    ,
                    data: [
                                {
                                    min: -404,
                                    max: -404,
                                    q1: -404,
                                    q3: -404,
                                    median: -404,
                                    mean: -404,
                                    items: [],
                                    outliers: [],
                                }
                            
                    ]
                }, 
                {
                    label: 'Tabu Search (favorite)',
                        borderWidth: 4
,
                    data: [
                                {
                                    min: -403,
                                    max: -403,
                                    q1: -403,
                                    q3: -403,
                                    median: -403,
                                    mean: -403,
                                    items: [],
                                    outliers: [],
                                }
                            
                    ]
                }, 
                {
                    label: 'Late Acceptance',
                        borderWidth: 1
                    ,
                    data: [
                                {
                                    min: -404,
                                    max: -404,
                                    q1: -404,
                                    q3: -404,
                                    median: -404,
                                    mean: -404,
                                    items: [],
                                    outliers: [],
                                }
                            
                    ]
                }, 
                {
                    label: 'Simulated Annealing',
                        borderWidth: 1
                    ,
                    data: [
                                {
                                    min: -404,
                                    max: -404,
                                    q1: -404,
                                    q3: -404,
                                    median: -404,
                                    mean: -404,
                                    items: [],
                                    outliers: [],
                                }
                            
                    ]
                }
        ]
    },
    options: {
        animation: false,
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            title: {
                display: true,
                text: 'Best hard score distribution summary (higher is better)'
            }
        },
        scales: {
            x: {
                display: true
            },
            y: {
                title: {
                    display: true,
                    text: 'Best hard score'
                },
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
  chart_bestScoreDistributionSummaryChart0_dba40.resize(1280, 720);
});
window.addEventListener('afterprint', () => {
  chart_bestScoreDistributionSummaryChart0_dba40.resize();
});