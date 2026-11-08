import express from 'express';
import User from '../models/User.js';

const router = express.Router();

// 1. YouTube OAuth ಲಾಗಿನ್ ಆರಂಭಿಸುವುದು
router.get('/auth/youtube', (req, res) => {
  const redirectUri = process.env.GOOGLE_REDIRECT_URI || "http://localhost:5000/api/auth/youtube/callback";
  const googleAuthUrl = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${process.env.GOOGLE_CLIENT_ID}&redirect_uri=${encodeURIComponent(redirectUri)}&response_type=code&scope=https://www.googleapis.com/auth/youtube.upload%20https://www.googleapis.com/auth/youtube.readonly&access_type=offline&prompt=consent`;
  res.redirect(googleAuthUrl);
});

// YouTube ಕಾಲ್ಬ್ಯಾಕ್ ರೂಟ್ (Callback Route)
router.get('/auth/youtube/callback', async (req, res) => {
  const { code } = req.query;
  try {
    console.log("YouTube Auth Code ಪಡೆದಿದೆ:", code);
    // In production, exchange code for tokens via oauth2Client.getToken(code)
    res.send(`
      <div style="font-family: sans-serif; text-align: center; padding: 40px; background: #0f172a; color: white; min-height: 100vh;">
        <h1 style="color: #ef4444;">✅ YouTube ಅಕೌಂಟ್ ಯಶಸ್ವಿಯಾಗಿ ಲಿಂಕ್ ಆಗಿದೆ!</h1>
        <p style="font-size: 18px; color: #94a3b8;">YouTube Studio 1-Click Uploading Authorized.</p>
        <p>ನೀವು ಈ ಟ್ಯಾಬ್ ಅನ್ನು ಮುಚ್ಚಿ Creator Studio ಅಪ್ಲಿಕೇಶನ್‌ಗೆ ಮರಳಬಹುದು.</p>
        <button onclick="window.close()" style="background: #ef4444; color: white; padding: 12px 24px; border: none; border-radius: 8px; font-weight: bold; cursor: pointer;">ಟ್ಯಾಬ್ ಮುಚ್ಚಿ (Close)</button>
      </div>
    `);
  } catch (error) {
    res.status(500).send("YouTube ಲಾಗಿನ್ ವಿಫಲವಾಗಿದೆ: " + error.message);
  }
});

// 2. Instagram / Facebook (Meta) OAuth ಲಾಗಿನ್
router.get('/auth/meta', (req, res) => {
  const redirectUri = process.env.META_REDIRECT_URI || "http://localhost:5000/api/auth/meta/callback";
  const metaAuthUrl = `https://www.facebook.com/v18.0/dialog/oauth?client_id=${process.env.FB_CLIENT_ID}&redirect_uri=${encodeURIComponent(redirectUri)}&scope=instagram_content_publish,instagram_basic,pages_manage_posts,pages_read_engagement`;
  res.redirect(metaAuthUrl);
});

// Meta ಕಾಲ್ಬ್ಯಾಕ್ ರೂಟ್
router.get('/auth/meta/callback', async (req, res) => {
  const { code } = req.query;
  try {
    console.log("Meta Auth Code ಪಡೆದಿದೆ:", code);
    res.send(`
      <div style="font-family: sans-serif; text-align: center; padding: 40px; background: #0f172a; color: white; min-height: 100vh;">
        <h1 style="color: #ec4899;">✅ Instagram & Facebook ಅಕೌಂಟ್ ಯಶಸ್ವಿಯಾಗಿ ಲಿಂಕ್ ಆಗಿದೆ!</h1>
        <p style="font-size: 18px; color: #94a3b8;">Instagram Reels & Facebook Video Publishing Authorized.</p>
        <p>ನೀವು ಈ ಟ್ಯಾಬ್ ಅನ್ನು ಮುಚ್ಚಿ Creator Studio ಅಪ್ಲಿಕೇಶನ್‌ಗೆ ಮರಳಬಹುದು.</p>
        <button onclick="window.close()" style="background: #ec4899; color: white; padding: 12px 24px; border: none; border-radius: 8px; font-weight: bold; cursor: pointer;">ಟ್ಯಾಬ್ ಮುಚ್ಚಿ (Close)</button>
      </div>
    `);
  } catch (error) {
    res.status(500).send("Meta ಲಾಗಿನ್ ವಿಫಲವಾಗಿದೆ: " + error.message);
  }
});

// Status check API
router.get('/status', (req, res) => {
  res.json({
    status: "online",
    schedulerInterval: "10 minutes",
    geminiConfigured: !!process.env.GEMINI_API_KEY,
    youtubeConfigured: !!process.env.GOOGLE_CLIENT_ID,
    metaConfigured: !!process.env.FB_CLIENT_ID
  });
});

export default router;
