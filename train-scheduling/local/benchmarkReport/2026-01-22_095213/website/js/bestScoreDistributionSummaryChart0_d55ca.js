
var chart_bestScoreDistributionSummaryChart0_d55ca = new Chart(document.getElementById('chart_bestScoreDistributionSummaryChart0_d55ca'), {
    type: 'boxplot',
    data: {
        labels: [
            'Problem_0'
        ],
        datasets: [
                {
                    label: 'Hill Climbing (favorite)',
                        borderWidth: 4
,
                    data: [
                                {
                                    min: -261,
                                    max: -261,
                                    q1: -261,
                                    q3: -261,
                                    median: -261,
                                    mean: -261,
                                    items: [],
                                    outliers: [],
                                }
                            
                    ]
                }, 
                {
                    label: 'Tabu Search',
                        borderWidth: 1
                    ,
                    data: [
                                {
                                    min: -314,
                                    max: -314,
                                    q1: -314,
                                    q3: -314,
                                    median: -314,
                                    mean: -314,
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
                                    min: -413,
                                    max: -413,
                                    q1: -413,
                                    q3: -413,
                                    median: -413,
                                    mean: -413,
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
                                    min: -270,
                                    max: -270,
                                    q1: -270,
                                    q3: -270,
                                    median: -270,
                                    mean: -270,
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
  chart_bestScoreDistributionSummaryChart0_d55ca.resize(1280, 720);
});
window.addEventListener('afterprint', () => {
  chart_bestScoreDistributionSummaryChart0_d55ca.resize();
});