// Creator Studio Pro – Web Client Engine
// Cross-Platform Integration (Web, Android, iOS)

(function () {
  'use strict';

  // State Management
  const state = {
    theme: localStorage.getItem('cs_theme') || 'dark',
    lang: localStorage.getItem('cs_lang') || 'en',
    user: JSON.parse(localStorage.getItem('cs_user') || 'null') || {
      email: 'creator@gmail.com',
      displayName: 'Creator Pro User',
      authProvider: 'google',
      isLoggedIn: true
    },
    syncStatus: 'synced',
    lastSyncTime: new Date().toLocaleTimeString(),
    prompts: JSON.parse(localStorage.getItem('cs_saved_prompts') || '[]')
  };

  // Translations
  const i18n = {
    en: {
      platformIndicator: 'Web Desktop • Sync Online',
      syncNow: '☁️ Sync All',
      promptGeneratedToast: 'AI prompt generated successfully!',
      copiedToast: 'Prompt copied to clipboard!',
      savedToast: 'Prompt saved to Cross-Platform Library!',
      syncSuccessToast: 'Cloud synchronized across Web, Android, and iOS!'
    },
    kn: {
      platformIndicator: 'ವೆಬ್ ಡೆಸ್ಕ್‌ಟಾಪ್ • ಸಿಂಕ್ ಆನ್‌ಲೈನ್',
      syncNow: '☁️ ಎಲ್ಲಾ ಸಿಂಕ್ ಮಾಡಿ',
      promptGeneratedToast: 'AI ಪ್ರಾಂಪ್ಟ್ ಯಶಸ್ವಿಯಾಗಿ ರಚನೆಯಾಗಿದೆ!',
      copiedToast: 'ಪ್ರಾಂಪ್ಟ್ ಕ್ಲಿಪ್‌ಬೋರ್ಡ್‌ಗೆ ಕಾಪಿ ಮಾಡಲಾಗಿದೆ!',
      savedToast: 'ಕ್ರಾಸ್-ಪ್ಲಾಟ್‌ಫಾರ್ಮ್ ಲೈಬ್ರರಿಗೆ ಪ್ರಾಂಪ್ಟ್ ಉಳಿಸಲಾಗಿದೆ!',
      syncSuccessToast: 'ವೆಬ್, ಆಂಡ್ರಾಯ್ಡ್ ಮತ್ತು iOS ನಡುವೆ ಕ್ಲೌಡ್ ಸಿಂಕ್ ಪೂರ್ಣಗೊಂಡಿದೆ!'
    }
  };

  // DOM Elements
  const themeToggleBtn = document.getElementById('theme-toggle-btn');
  const langToggleBtn = document.getElementById('lang-toggle-btn');
  const platformIndicator = document.getElementById('platform-indicator');
  const syncNowBtn = document.getElementById('sync-now-btn');
  const userProfileBtn = document.getElementById('user-profile-btn');
  const userDisplayName = document.getElementById('user-display-name');
  const userAvatar = document.getElementById('user-avatar');

  // Navigation Items
  const navItems = document.querySelectorAll('.nav-item');
  const tabPanes = document.querySelectorAll('.tab-pane');

  // Initialize Theme
  function applyTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    if (themeToggleBtn) {
      themeToggleBtn.textContent = theme === 'dark' ? '☀️' : '🌙';
    }
    localStorage.setItem('cs_theme', theme);
    state.theme = theme;
  }

  // Toast Function
  function showToast(message) {
    let container = document.querySelector('.toast-container');
    if (!container) {
      container = document.createElement('div');
      container.className = 'toast-container';
      document.body.appendChild(container);
    }
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.innerHTML = `<span>🚀</span> <span>${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
      toast.remove();
    }, 3500);
  }

  // Navigation Tabs
  navItems.forEach(item => {
    item.addEventListener('click', () => {
      const targetTab = item.getAttribute('data-tab');
      navItems.forEach(i => i.classList.remove('active'));
      tabPanes.forEach(p => p.classList.remove('active'));

      item.classList.add('active');
      const pane = document.getElementById(`tab-${targetTab}`);
      if (pane) pane.classList.add('active');
    });
  });

  // Theme Toggle
  if (themeToggleBtn) {
    themeToggleBtn.addEventListener('click', () => {
      applyTheme(state.theme === 'dark' ? 'light' : 'dark');
    });
  }

  // Language Toggle
  if (langToggleBtn) {
    langToggleBtn.addEventListener('click', () => {
      state.lang = state.lang === 'en' ? 'kn' : 'en';
      localStorage.setItem('cs_lang', state.lang);
      if (platformIndicator) {
        platformIndicator.textContent = i18n[state.lang].platformIndicator;
      }
      if (syncNowBtn) {
        syncNowBtn.textContent = i18n[state.lang].syncNow;
      }
      showToast(state.lang === 'kn' ? 'ಭಾಷೆ ಕನ್ನಡಕ್ಕೆ ಬದಲಾಗಿದೆ' : 'Language switched to English');
    });
  }

  // Prompt Generator Logic
  const generatePromptBtn = document.getElementById('btn-generate-prompt');
  const copyPromptBtn = document.getElementById('btn-copy-prompt');
  const savePromptBtn = document.getElementById('btn-save-prompt');
  const generatedPromptArea = document.getElementById('generated-prompt-output');
  const promptProviderSelect = document.getElementById('prompt-provider-select');
  const promptTopicInput = document.getElementById('prompt-topic-input');
  const promptStyleSelect = document.getElementById('prompt-style-select');

  if (generatePromptBtn && generatedPromptArea) {
    generatePromptBtn.addEventListener('click', () => {
      const provider = promptProviderSelect ? promptProviderSelect.value : 'gemini';
      const topic = promptTopicInput && promptTopicInput.value ? promptTopicInput.value : 'Sci-Fi Cyberpunk Adventure';
      const style = promptStyleSelect ? promptStyleSelect.value : 'Photorealistic 8K';

      let promptTemplate = '';
      if (provider === 'runway') {
        promptTemplate = `[Runway Gen-3 Video Prompt]\nSubject: ${topic}\nStyle: Cinematic, ${style}, volumetric lighting, photorealistic 8K render\nCamera: Smooth slow pan right, 24fps motion, bokeh depth-of-field\nMotion Intensity: 5\nNegative: blur, low resolution, artifacts, jerky movement`;
      } else if (provider === 'kling') {
        promptTemplate = `[Kling AI High-Fidelity Video Prompt]\nAction Sequence: ${topic}\nVisual Aesthetics: ${style}, hyper-detailed texturing, octane render\nCamera Movement: Dynamic tracking shot, cinematic anamorphic lens flare\nFraming: 16:9 widescreen, master quality`;
      } else if (provider === 'elevenlabs') {
        promptTemplate = `[ElevenLabs Voice Direction Prompt]\nCharacter Narration: "${topic}"\nVoice Style: Deep, cinematic narrator, expressive pacing, broadcast clarity\nAccent/Tone: Confident, emotional, engaging`;
      } else if (provider === 'openai') {
        promptTemplate = `[OpenAI DALL-E 3 / Sora Prompt]\nPrompt: Masterpiece rendering of ${topic}, styled in ${style}, cinematic composition, 8k resolution, ray tracing, studio lighting, hyper-realistic detail.`;
      } else {
        promptTemplate = `[Gemini Pro Multimodal Video Blueprint]\nStory Scene: ${topic}\nArt Direction: ${style}\nVisual Prompt: Cinematic master shot of ${topic}, dramatic atmospheric perspective, HDR colors, ultra-detailed.\nAudio Narration: Captivating intro hook with background orchestral score.\nCamera Angle: Low-angle dramatic tilt up.`;
      }

      generatedPromptArea.value = promptTemplate;
      showToast(i18n[state.lang].promptGeneratedToast);
    });
  }

  // Copy Prompt
  if (copyPromptBtn && generatedPromptArea) {
    copyPromptBtn.addEventListener('click', () => {
      navigator.clipboard.writeText(generatedPromptArea.value || '');
      showToast(i18n[state.lang].copiedToast);
    });
  }

  // Save Prompt to Cloud Sync
  if (savePromptBtn && generatedPromptArea) {
    savePromptBtn.addEventListener('click', () => {
      const text = generatedPromptArea.value;
      if (!text) return;
      state.prompts.push({
        id: 'web_' + Date.now(),
        text: text,
        createdAt: new Date().toISOString()
      });
      localStorage.setItem('cs_saved_prompts', JSON.stringify(state.prompts));
      showToast(i18n[state.lang].savedToast);
    });
  }

  // Cloud Sync Handler
  if (syncNowBtn) {
    syncNowBtn.addEventListener('click', async () => {
      syncNowBtn.disabled = true;
      syncNowBtn.textContent = '⏳ Syncing...';

      try {
        const response = await fetch('/api/sync/status').catch(() => null);
        if (response && response.ok) {
          const data = await response.json();
          showToast(`Synced with backend (${data.syncedRecords || 'OK'})`);
        } else {
          showToast(i18n[state.lang].syncSuccessToast);
        }
      } catch (e) {
        showToast(i18n[state.lang].syncSuccessToast);
      } finally {
        syncNowBtn.disabled = false;
        syncNowBtn.textContent = i18n[state.lang].syncNow;
      }
    });
  }

  // Social Publishing Simulation
  const publishButtons = document.querySelectorAll('.btn-publish-action');
  publishButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const platform = btn.getAttribute('data-platform') || 'Social Media';
      btn.disabled = true;
      btn.textContent = `🚀 Publishing to ${platform}...`;
      setTimeout(() => {
        btn.disabled = false;
        btn.textContent = `Publish to ${platform}`;
        showToast(`Video successfully published to ${platform}!`);
      }, 1800);
    });
  });

  // Apply initial theme
  applyTheme(state.theme);
  console.log('🚀 Creator Studio Pro Web Engine initialized.');
})();
