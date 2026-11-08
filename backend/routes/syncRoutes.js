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

export default router;
