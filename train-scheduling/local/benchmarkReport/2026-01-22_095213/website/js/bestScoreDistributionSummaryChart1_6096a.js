
var chart_bestScoreDistributionSummaryChart1_6096a = new Chart(document.getElementById('chart_bestScoreDistributionSummaryChart1_6096a'), {
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
                                    min: 52162,
                                    max: 52162,
                                    q1: 52162,
                                    q3: 52162,
                                    median: 52162,
                                    mean: 52162,
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
                                    min: 51776,
                                    max: 51776,
                                    q1: 51776,
                                    q3: 51776,
                                    median: 51776,
                                    mean: 51776,
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
                                    min: 44442,
                                    max: 44442,
                                    q1: 44442,
                                    q3: 44442,
                                    median: 44442,
                                    mean: 44442,
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
                                    min: 48989,
                                    max: 48989,
                                    q1: 48989,
                                    q3: 48989,
                                    median: 48989,
                                    mean: 48989,
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
  chart_bestScoreDistributionSummaryChart1_6096a.resize(1280, 720);
});
window.addEventListener('afterprint', () => {
  chart_bestScoreDistributionSummaryChart1_6096a.resize();
});