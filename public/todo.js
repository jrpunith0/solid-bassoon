(() => {
  'use strict';

  const STORAGE_KEY = 'creator-studio-todos-v1';
  const form = document.getElementById('add-form');
  const input = document.getElementById('task-input');
  const list = document.getElementById('task-list');
  const emptyState = document.getElementById('empty-state');
  const count = document.getElementById('task-count');
  const summary = document.getElementById('completion-summary');
  const clearButton = document.getElementById('clear-completed');
  const aiForm = document.getElementById('ai-plan-form');
  const aiGoal = document.getElementById('ai-goal');
  const aiButton = document.getElementById('ai-plan-button');
  const aiStatus = document.getElementById('ai-status');
  const aiSuggestions = document.getElementById('ai-suggestions');
  const filterButtons = [...document.querySelectorAll('[data-filter]')];
  let filter = 'all';
  let tasks = loadTasks();

  function loadTasks() {
    try {
      const stored = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');
      if (!Array.isArray(stored)) return [];
      return stored.filter(task => task && typeof task.id === 'string' && typeof task.text === 'string')
        .map(task => ({ id: task.id, text: task.text, completed: Boolean(task.completed), createdAt: Number(task.createdAt) || Date.now() }));
    } catch (error) {
      console.warn('Could not read saved tasks from local storage.', error);
      return [];
    }
  }

  function saveTasks() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(tasks));
    } catch (error) {
      console.error('Could not save tasks to local storage.', error);
      summary.textContent = 'Could not save changes in this browser.';
    }
  }

  function createId() {
    return typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function'
      ? crypto.randomUUID()
      : `${Date.now()}-${Math.random().toString(36).slice(2)}`;
  }

  function addTask(text) {
    const cleanText = String(text || '').trim().slice(0, 200);
    if (!cleanText || tasks.some(task => task.text.toLowerCase() === cleanText.toLowerCase())) return false;
    tasks.unshift({ id: createId(), text: cleanText, completed: false, createdAt: Date.now() });
    saveTasks();
    render();
    return true;
  }

  function renderSuggestions(suggestions) {
    aiSuggestions.replaceChildren();
    suggestions.forEach(suggestion => {
      const item = document.createElement('li');
      item.className = 'ai-suggestion';
      const text = document.createElement('span');
      text.textContent = suggestion;
      const add = document.createElement('button');
      add.type = 'button';
      add.textContent = 'Add task';
      add.addEventListener('click', () => {
        if (addTask(suggestion)) {
          item.remove();
          if (!aiSuggestions.children.length) aiSuggestions.hidden = true;
        } else {
          add.textContent = 'Already added';
          add.disabled = true;
        }
      });
      item.append(text, add);
      aiSuggestions.append(item);
    });
    aiSuggestions.hidden = suggestions.length === 0;
  }

  function render() {
    const visibleTasks = tasks.filter(task => filter === 'all' || (filter === 'active' ? !task.completed : task.completed));
    const remaining = tasks.filter(task => !task.completed).length;
    const completed = tasks.length - remaining;

    list.replaceChildren();
    for (const task of visibleTasks) {
      const item = document.createElement('li');
      item.className = `task-item${task.completed ? ' is-completed' : ''}`;

      const toggle = document.createElement('button');
      toggle.type = 'button';
      toggle.className = 'task-check';
      toggle.setAttribute('aria-label', task.completed ? `Mark ${task.text} as active` : `Complete ${task.text}`);
      toggle.setAttribute('aria-pressed', String(task.completed));
      toggle.addEventListener('click', () => {
        task.completed = !task.completed;
        saveTasks();
        render();
      });

      const text = document.createElement('span');
      text.className = 'task-text';
      text.textContent = task.text;

      const remove = document.createElement('button');
      remove.type = 'button';
      remove.className = 'delete-button';
      remove.textContent = '×';
      remove.setAttribute('aria-label', `Delete ${task.text}`);
      remove.addEventListener('click', () => {
        tasks = tasks.filter(candidate => candidate.id !== task.id);
        saveTasks();
        render();
      });

      item.append(toggle, text, remove);
      list.append(item);
    }

    emptyState.hidden = visibleTasks.length !== 0;
    count.textContent = `${remaining} ${remaining === 1 ? 'task' : 'tasks'} left`;
    summary.textContent = tasks.length === 0 ? 'A fresh start.' : `${completed} of ${tasks.length} completed`;
    clearButton.disabled = completed === 0;
  }

  form.addEventListener('submit', event => {
    event.preventDefault();
    if (addTask(input.value)) input.value = '';
    input.focus();
  });

  aiForm.addEventListener('submit', async event => {
    event.preventDefault();
    const goal = aiGoal.value.trim();
    if (!goal) return;

    aiButton.disabled = true;
    aiButton.textContent = 'Thinking…';
    aiStatus.classList.remove('is-error');
    aiStatus.textContent = 'ChatGPT is turning your goal into manageable steps…';
    aiSuggestions.hidden = true;
    try {
      const response = await fetch('/api/sync/chatgpt/plan', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          goal,
          tasks: tasks.filter(task => !task.completed).slice(0, 30).map(task => task.text)
        })
      });
      const result = await response.json().catch(() => ({}));
      if (!response.ok) throw new Error(result.message || `Request failed (${response.status}).`);
      const suggestions = Array.isArray(result.suggestions) ? result.suggestions : [];
      renderSuggestions(suggestions);
      aiStatus.textContent = suggestions.length
        ? 'Choose the steps you want to add. They’ll be saved with your other tasks.'
        : 'No task suggestions came back. Try describing your goal in a different way.';
    } catch (error) {
      aiStatus.classList.add('is-error');
      aiStatus.textContent = error.message.includes('Failed to fetch')
        ? 'Could not reach the app server. Start the backend and try again.'
        : error.message;
    } finally {
      aiButton.disabled = false;
      aiButton.innerHTML = '<span aria-hidden="true">✦</span> Make a plan';
    }
  });

  filterButtons.forEach(button => {
    button.addEventListener('click', () => {
      filter = button.dataset.filter;
      filterButtons.forEach(option => {
        const selected = option === button;
        option.classList.toggle('is-active', selected);
        option.setAttribute('aria-pressed', String(selected));
      });
      render();
    });
  });

  clearButton.addEventListener('click', () => {
    tasks = tasks.filter(task => !task.completed);
    saveTasks();
    render();
  });

  window.addEventListener('storage', event => {
    if (event.key === STORAGE_KEY || event.key === null) {
      tasks = loadTasks();
      render();
    }
  });

  render();
})();
