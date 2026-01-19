
var chart_bestScorePerTimeSpentSummaryChart0_53aec = new Chart(document.getElementById('chart_bestScorePerTimeSpentSummaryChart0_53aec'), {
    type: 'line',
    data: {
        datasets: [
            {
                  label: 'Hill Climbing',
                    borderWidth: 1
                  ,
                  data: [
                    {x: 60128, y: -404}
                  ]
                }, 
{
                  label: 'Tabu Search (favorite)',
                    borderWidth: 4
,
                  data: [
                    {x: 60143, y: -403}
                  ]
                }, 
{
                  label: 'Late Acceptance',
                    borderWidth: 1
                  ,
                  data: [
                    {x: 60131, y: -404}
                  ]
                }, 
{
                  label: 'Simulated Annealing',
                    borderWidth: 1
                  ,
                  data: [
                    {x: 60125, y: -404}
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
                text: 'Best hard score per time spent summary (higher left is better)'
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
                suggestedMax: 60143,
                type: 'linear',
                display: true
            },
            y: {
                title: {
                    display: true,
                    text: 'Best hard score'
                },
                ticks: {
                        stepSize: 10
                        
                },
                type: 'linear',
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
  chart_bestScorePerTimeSpentSummaryChart0_53aec.resize(1280, 720);
});
window.addEventListener('afterprint', () => {
  chart_bestScorePerTimeSpentSummaryChart0_53aec.resize();
});
