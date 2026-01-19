
var chart_worstScoreDifferencePercentageSummaryChart1_8482d = new Chart(document.getElementById('chart_worstScoreDifferencePercentageSummaryChart1_8482d'), {
    type: 'bar',
    data: {
        labels: [
            'Problem_0'
        ],
        datasets: [
            {
                  label: 'Hill Climbing',
                  grouped: true,
                    borderWidth: 1
                  ,
                  data: [
                    0.5800223722915027
                  ]
                }, 
{
                  label: 'Tabu Search (favorite)',
                  grouped: true,
                    borderWidth: 4
,
                  data: [
                    0.14086257612793637
                  ]
                }, 
{
                  label: 'Late Acceptance',
                  grouped: true,
                    borderWidth: 1
                  ,
                  data: [
                    0
                  ]
                }, 
{
                  label: 'Simulated Annealing',
                  grouped: true,
                    borderWidth: 1
                  ,
                  data: [
                    0
                  ]
                }
        ]
    },
    options: {
        animation: false,
        responsive: true,
        maintainAspectRatio: false,
        resizeDelay: 100,
        spanGaps: true,
        plugins: {
            title: {
                display: true,
                text: 'Worst soft score difference percentage summary (higher is better)'
            }
        },
        scales: {
            x: {
                display: true
            },
            y: {
                title: {
                    display: true,
                    text: 'Worst soft score difference percentage'
                },
                ticks: {
                        stepSize: 0.01
                        
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
  chart_worstScoreDifferencePercentageSummaryChart1_8482d.resize(1280, 720);
});
window.addEventListener('afterprint', () => {
  chart_worstScoreDifferencePercentageSummaryChart1_8482d.resize();
});