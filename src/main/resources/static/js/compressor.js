async function loadCurrentValues() {
  const response = await fetch('/api/current-values');
  if (!response.ok) return;
  const data = await response.json();

  document.querySelectorAll('tr[data-group]').forEach(row => {
    const group = row.dataset.group;   // oil, coolant, gas-1 ...
    const param = row.dataset.param;   // "Температура масла", "Давление масла" ...
    const value = data[group] && data[group][param];

    if (value != null) {
      row.querySelector('.current-value').textContent = value;
    }
  });
}

document.addEventListener('DOMContentLoaded', () => {
  loadCurrentValues();
  setInterval(loadCurrentValues, 1000);
});



document.addEventListener('DOMContentLoaded', () => {
        const tabButtons = document.querySelectorAll('.top-tabs .tab-btn');
        const tabPanes = document.querySelectorAll('.tab-pane');

        tabButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                const targetId = btn.dataset.target;
                tabButtons.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                tabPanes.forEach(p => p.classList.toggle('active', p.id === targetId));
            });
        });
    });