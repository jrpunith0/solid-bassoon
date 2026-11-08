import express from 'express';

const router = express.Router();

// Mock in-memory storage for syncing across Web, Android, and iOS
let syncedProjects = [
  {
    id: "proj_01",
    name: "AI Cyberpunk Manga Ep. 1",
    category: "Manga Story to Video",
    aspectRatio: "16:9",
    resolution: "1080p",
    scenesCount: 6,
    updatedAt: new Date().toISOString()
  },
  {
    id: "proj_02",
    name: "Karnataka Wildlife Documentary Shorts",
    category: "YouTube Shorts",
    aspectRatio: "9:16",
    resolution: "1080p",
    scenesCount: 4,
    updatedAt: new Date().toISOString()
  }
];

let syncedPrompts = [
  {
    id: "prompt_01",
    provider: "gemini",
    topic: "Ancient Hampi Ruins in Monsoon",
    promptText: "Cinematic 8K drone sweep across ancient Hampi stone architecture during golden hour monsoon, mist rolling over boulders, volumetric lighting, photorealistic.",
    updatedAt: new Date().toISOString()
  }
];

let syncedCharacters = [
  {
    id: "char_01",
    name: "Aarya",
    role: "Hero",
    visualStyle: "Anime Cinematic",
    clothingStyle: "Traditional futuristic armor",
    updatedAt: new Date().toISOString()
  }
];

// GET /api/sync/status
router.get('/status', (req, res) => {
  res.json({
    status: "ok",
    syncService: "Creator Studio Pro Universal Sync Engine",
    supportedPlatforms: ["Website (Web/PWA)", "Android App (APK/AAB)", "iOS App (iPhone/iPad)"],
    syncedRecords: `${syncedProjects.length} Projects, ${syncedPrompts.length} Prompts, ${syncedCharacters.length} Characters`,
    serverTime: new Date().toISOString()
  });
});

// GET /api/sync/projects
router.get('/projects', (req, res) => {
  res.json({ success: true, projects: syncedProjects });
});

// POST /api/sync/projects
router.post('/projects', (req, res) => {
  const project = req.body;
  if (!project.id) project.id = "proj_" + Date.now();
  project.updatedAt = new Date().toISOString();
  
  const existingIdx = syncedProjects.findIndex(p => p.id === project.id);
  if (existingIdx >= 0) {
    syncedProjects[existingIdx] = project;
  } else {
    syncedProjects.push(project);
  }
  res.json({ success: true, project });
});

// GET /api/sync/prompts
router.get('/prompts', (req, res) => {
  res.json({ success: true, prompts: syncedPrompts });
});

// POST /api/sync/prompts
router.post('/prompts', (req, res) => {
  const prompt = req.body;
  if (!prompt.id) prompt.id = "prompt_" + Date.now();
  prompt.updatedAt = new Date().toISOString();
  
  const existingIdx = syncedPrompts.findIndex(p => p.id === prompt.id);
  if (existingIdx >= 0) {
    syncedPrompts[existingIdx] = prompt;
  } else {
    syncedPrompts.push(prompt);
  }
  res.json({ success: true, prompt });
});

// GET /api/sync/characters
router.get('/characters', (req, res) => {
  res.json({ success: true, characters: syncedCharacters });
});

// POST /api/sync/characters
router.post('/characters', (req, res) => {
  const character = req.body;
  if (!character.id) character.id = "char_" + Date.now();
  character.updatedAt = new Date().toISOString();
  
  const existingIdx = syncedCharacters.findIndex(c => c.id === character.id);
  if (existingIdx >= 0) {
    syncedCharacters[existingIdx] = character;
  } else {
    syncedCharacters.push(character);
  }
  res.json({ success: true, character });
});

// POST /api/sync/auth (Universal login verification for Web, Android, iOS)
router.post('/auth', (req, res) => {
  const { provider, email, phone, otp } = req.body;
  if (provider === 'google') {
    return res.json({
      success: true,
      token: "jwt_creator_studio_google_" + Date.now(),
      user: {
        email: email || "creator@gmail.com",
        name: "Creator Pro User",
        authProvider: "google"
      }
    });
  } else if (provider === 'phone') {
    if (otp && otp.length === 6) {
      return res.json({
        success: true,
        token: "jwt_creator_studio_phone_" + Date.now(),
        user: {
          phone: phone || "+919876543210",
          name: "Mobile Creator",
          authProvider: "phone"
        }
      });
    } else {
      return res.status(400).json({ success: false, message: "Invalid 6-digit OTP" });
    }
  }
  return res.json({
    success: true,
    user: {
      email: email || "user@creatorstudio.pro",
      name: "Creator Pro User",
      authProvider: provider || "email"
    }
  });
});

// POST /api/sync/chatgpt/plan — keep the OpenAI API key on the server.
router.post('/chatgpt/plan', async (req, res) => {
  const apiKey = process.env.OPENAI_API_KEY;
  if (!apiKey) {
    return res.status(503).json({ message: 'ChatGPT planning is not configured. Set OPENAI_API_KEY in the backend environment.' });
  }

  const goal = typeof req.body?.goal === 'string' ? req.body.goal.trim().slice(0, 1000) : '';
  const tasks = Array.isArray(req.body?.tasks)
    ? req.body.tasks.filter(task => typeof task === 'string').slice(0, 30).map(task => task.slice(0, 200))
    : [];
  if (!goal) return res.status(400).json({ message: 'Describe a goal to create a task plan.' });

  try {
    const response = await fetch('https://api.openai.com/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${apiKey}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        model: process.env.OPENAI_MODEL || 'gpt-4o-mini',
        temperature: 0.4,
        response_format: { type: 'json_object' },
        messages: [
          {
            role: 'system',
            content: 'You are a practical task-planning assistant. Break the user goal into 3 to 8 concise, specific, actionable task titles. Avoid duplicating existing active tasks. Return only a JSON object with a "suggestions" property containing an array of strings. Do not include markdown.'
          },
          {
            role: 'user',
            content: JSON.stringify({ goal, existingActiveTasks: tasks })
          }
        ]
      })
    });

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
      console.error('OpenAI task planner request failed:', response.status, data.error?.type || 'unknown error');
      const status = response.status === 401 ? 503 : 502;
      return res.status(status).json({
        message: response.status === 401
          ? 'The configured OpenAI API key was rejected. Check OPENAI_API_KEY on the backend.'
          : 'OpenAI could not create a plan right now. Please try again.'
      });
    }

    let plan;
    try {
      plan = JSON.parse(data.choices?.[0]?.message?.content || '{}');
    } catch {
      return res.status(502).json({ message: 'The AI returned an unreadable plan. Please try again.' });
    }
    const suggestions = Array.isArray(plan.suggestions)
      ? [...new Set(plan.suggestions.filter(item => typeof item === 'string').map(item => item.trim()).filter(Boolean))].slice(0, 8).map(item => item.slice(0, 200))
      : [];
    return res.json({ suggestions });
  } catch (error) {
    console.error('OpenAI task planner unavailable:', error.message);
    return res.status(502).json({ message: 'Could not reach OpenAI. Check the backend connection and try again.' });
  }
});

export default router;
