if (Notification.permission !== 'granted' && Notification.permission !== 'denied') {
    Notification.requestPermission();
}

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
      currentValues: data.values,
      warnings: data.warnings || []
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
  const { state, workHours, values, warnings = [] } = data;


  const compressorLi = document.querySelector(`[data-compressor-id="${compressorId}"]`);
  if (compressorLi) {
    if (warnings.length > 0  || data.state === 'fall') {

      compressorLi.classList.add('warning-compressor');
      notifyAlarm();
    } else {
      compressorLi.classList.remove('warning-compressor');
    }
  }


  const compressorScreen = document.getElementById('screen-compressor');
  if (!compressorScreen) return;

  const workHoursEl = document.querySelector("div[data-group='workHours']");
  if (workHoursEl) {
    workHoursEl.textContent = 'Наработка часов: ' + workHours.toFixed(2);
  }

  const groupToButton = {
    'oil': 'tab-oil',
    'coolant': 'tab-coolant',
    'gas-1': 'tab-gas',
    'gas-2': 'tab-gas',
    'gas-3': 'tab-gas',
    'gas-4': 'tab-gas',
    'IVG': 'tab-bku',
    'vibration': 'tab-bku',
    'gas-pollution': 'tab-bku',
    'tempBKU': 'tab-bku',
  };

  document.querySelectorAll('tr[data-group]').forEach(row => {
    const group = row.dataset.group;
    const param = row.dataset.param;
    const value = values[group] && values[group][param];

    if (value != null) {
      row.querySelector('.current-value').textContent = value;
    }

    if (warnings && warnings.includes(param)) {
      row.classList.add('warning-row');

      const targetId = groupToButton[group];

      if (targetId) {
        const button = document.querySelector(`[data-target="${targetId}"]`);
        if (button) {
          button.classList.add('warning-tab');
        }
      }
    } else {
      row.classList.remove('warning-row');
    }
  });

  Object.entries(groupToButton).forEach(([group, targetId]) => {
    const rowsInGroup = document.querySelectorAll(`tr[data-group="${group}"]`);
    const hasWarningInGroup = Array.from(rowsInGroup).some(row =>
      warnings && warnings.includes(row.dataset.param)
    );

    const button = document.querySelector(`[data-target="${targetId}"]`);
    if (button) {
      if (hasWarningInGroup) {
        button.classList.add('warning-tab');

      } else {
        button.classList.remove('warning-tab');
      }
    }
  });



  const stateLabel = document.getElementById('compressor-state-label');
  if (stateLabel) {
    stateLabel.textContent = 'Состояние: ' +
      (state === 'working' ? 'В РАБОТЕ' :
       state === 'off' ? 'ВЫКЛЮЧЕН' :
       state === 'fall' ? 'АВАРИЯ' : 'нет данных');
  }

  const resetButton = document.getElementById('reset');
    if (resetButton) {
        if (data.state === 'fall') {
            resetButton.classList.add('warning');
            stateLabel.classList.add('warning');
        } else {
            resetButton.classList.remove('warning');
            stateLabel.classList.remove('warning');
        }
    }



}

document.addEventListener('DOMContentLoaded', () => {
  initCompressorsFromDom();
  if (compressors.size > 0) {
    initWebSocket();
  }
});


function notifyAlarm() {
    beep();
    if (document.hidden) {

        if (Notification.permission === 'granted') {
            try {
                new Notification('⚠️ Авария на компрессоре', {
                    body: 'Компрессор в аварийном состоянии!',
                    silent: false,
                });
            } catch (e) {
                console.warn('Ошибка показа уведомления:', e);
            }
        } else if (Notification.permission !== 'denied') {

            Notification.requestPermission().then(perm => {
                if (perm === 'granted') {

                    notifyAlarm();
                }
            });
        }
    }
}

function beep() {
    try {
        const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        const oscillator = audioCtx.createOscillator();
        const gainNode = audioCtx.createGain();
        oscillator.connect(gainNode);
        gainNode.connect(audioCtx.destination);
        oscillator.frequency.value = 2000;
        gainNode.gain.value = 0.1;
        oscillator.start();
        oscillator.stop(audioCtx.currentTime + 0.3);
    } catch (e) {
        console.log('Звук не поддерживается', e);
    }
}