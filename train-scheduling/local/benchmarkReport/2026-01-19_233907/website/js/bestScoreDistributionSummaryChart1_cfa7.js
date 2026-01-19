
var chart_bestScoreDistributionSummaryChart1_cfa7 = new Chart(document.getElementById('chart_bestScoreDistributionSummaryChart1_cfa7'), {
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
                                    min: 48554,
                                    max: 48554,
                                    q1: 48554,
                                    q3: 48554,
                                    median: 48554,
                                    mean: 48554,
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
                                    min: 48342,
                                    max: 48342,
                                    q1: 48342,
                                    q3: 48342,
                                    median: 48342,
                                    mean: 48342,
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
                                    min: 48274,
                                    max: 48274,
                                    q1: 48274,
                                    q3: 48274,
                                    median: 48274,
                                    mean: 48274,
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
                                    min: 48274,
                                    max: 48274,
                                    q1: 48274,
                                    q3: 48274,
                                    median: 48274,
                                    mean: 48274,
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
                text: 'Best soft score distribution summary (higher is better)'
            }
        },
        scales: {
            x: {
                display: true
            },
            y: {
                title: {
                    display: true,
                    text: 'Best soft score'
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
  chart_bestScoreDistributionSummaryChart1_cfa7.resize(1280, 720);
});
window.addEventListener('afterprint', () => {
  chart_bestScoreDistributionSummaryChart1_cfa7.resize();
});