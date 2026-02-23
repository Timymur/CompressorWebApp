const compressors = new Map();

function initCompressorsFromDom() {
  document.querySelectorAll('[data-compressor-id]').forEach(el => {
    const id = Number(el.dataset.compressorId);
    if (!compressors.has(id)) {
      compressors.set(id, {
        currentValues: {}
      });
    }
  });

  const root = document.getElementById('compressor-root');
  if (root) {
    const id = Number(root.dataset.compressorId);
    if (!compressors.has(id)) {
      compressors.set(id, {
        currentValues: {}
      });
    }
  }
}

let socket = null;

function initWebSocket() {
  if (socket && socket.readyState === WebSocket.OPEN) return;

  socket = new WebSocket("ws://localhost:8080/ws/compressor");

  socket.onopen = () => {
    console.log("WS connected");
    compressors.forEach((data, id) => {
      sendRequest(id);
    });
  };

  socket.onmessage = (event) => {
    const data = JSON.parse(event.data);
    const id = data.compressorId;

    compressors.set(id, {
      workHours: data.workHours,
      currentValues: data.values
    });

    updateUiForCompressor(id, data);

    setTimeout(() => {
      sendRequest(id);
    }, 3000);
  };

  socket.onclose = () => console.log("WS closed");
  socket.onerror = (err) => console.error("WS error", err);
}

function sendRequest(compressorId) {
  if (!socket || socket.readyState !== WebSocket.OPEN) return;

  socket.send(JSON.stringify({
    compressorId: compressorId
  }));
}

function updateUiForCompressor(compressorId, data) {
  const { state, workHours, values, warnings } = data;

  const compressorScreen = document.getElementById('screen-compressor');
  if (!compressorScreen) return;

  const workHoursEl = document.querySelector("div[data-group='workHours']");
  if (workHoursEl) {
    workHoursEl.textContent = 'Наработка часов: ' + workHours.toFixed(2);
  }

  document.querySelectorAll('tr[data-group]').forEach(row => {
    const group = row.dataset.group;
    const param = row.dataset.param;
    const value = values[group] && values[group][param];

    if (value != null) {
      row.querySelector('.current-value').textContent = value;
    }

    if (warnings && warnings.includes(param)) {
      row.classList.add('warning-row');
    } else {
      row.classList.remove('warning-row');
    }
  });

  const stateLabel = document.getElementById('compressor-state-label');
  if (stateLabel) {
    stateLabel.textContent = 'Состояние: ' +
      (state === 'working' ? 'В РАБОТЕ' :
       state === 'off' ? 'ВЫКЛЮЧЕН' :
       state === 'fall' ? 'АВАРИЯ' : 'нет данных');
  }

}

document.addEventListener('DOMContentLoaded', () => {
  initCompressorsFromDom();
  if (compressors.size > 0) {
    initWebSocket();
  }

});
