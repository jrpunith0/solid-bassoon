# Creator Studio - Automated 10-Minute Video Generator & Uploader Backend

## 🌟 ವೈಶಿಷ್ಟ್ಯಗಳು (Features)
1. **Google AI Studio Gemini Script Engine**: ಪ್ರತಿ 10 ನಿಮಿಷಕ್ಕೆ ಆಕರ್ಷಕ ಸ್ಕ್ರಿಪ್ಟ್, ಶೀರ್ಷಿಕೆ ಮತ್ತು 3 ದೃಶ್ಯ ಪ್ರಾಂಪ್ಟ್ ಜನರೇಟ್ ಮಾಡುತ್ತದೆ.
2. **Text-To-Speech (TTS)**: ಧ್ವನಿ ವಾಯ್ಸ್‌ಓವರ್ ಸಿದ್ಧಪಡಿಸುತ್ತದೆ.
3. **Pexels & AI Scene Fetcher**: ವೀಡಿಯೊಗಾಗಿ 3 ಉತ್ತಮ ಗುಣಮಟ್ಟದ ದೃಶ್ಯಗಳನ್ನು ಡೌನ್‌ಲೋಡ್ ಮಾಡುತ್ತದೆ.
4. **FFmpeg Video Renderer**: ಚಿತ್ರಗಳು ಮತ್ತು ಆಡಿಯೋ ಸೇರಿಸಿ 1080p MP4 ವೀಡಿಯೊ ರೆಂಡರ್ ಮಾಡುತ್ತದೆ.
5. **YouTube & Meta OAuth Publishing**: YouTube Shorts, Instagram Reels ಮತ್ತು Facebook ಗೆ ಆಟೋ-ಅಪ್‌ಲೋಡ್ ಮಾಡುತ್ತದೆ.
6. **Crash-Proof Cron Scheduler**: ಪ್ರತಿ 10 ನಿಮಿಷಕ್ಕೊಮ್ಮೆ ನಿರಂತರವಾಗಿ ಹಿನ್ನೆಲೆಯಲ್ಲಿ ರನ್ ಆಗುತ್ತದೆ.

---

## 🚀 ಸೆಟಪ್ ವಿಧಾನ (Setup & Installation)

### 1. ಅಗತ್ಯವಿರುವ ಸಾಫ್ಟ್‌ವೇರ್ (Prerequisites)
- **Node.js**: v18+
- **FFmpeg**:
  - Ubuntu/Debian: `sudo apt-get install ffmpeg`
  - macOS: `brew install ffmpeg`
  - Windows: `winget install Gyan.FFmpeg`
- **MongoDB**: Local MongoDB ಅಥವಾ MongoDB Atlas Cloud

### 2. ಇನ್‌ಸ್ಟಾಲೇಶನ್ (Install Dependencies)
```bash
cd backend
npm install
```

### 3. ಎನ್ವಿರಾನ್‌ಮೆಂಟ್ ಕಾನ್ಫಿಗರೇಶನ್ (Configure .env)
`.env.example` ಅನ್ನು `.env` ಎಂದು ನಕಲಿಸಿ ಮತ್ತು ನಿಮ್ಮ ಕೀಗಳನ್ನು ನಮೂದಿಸಿ:
```bash
cp .env.example .env
```

### 4. ಸರ್ವರ್ ಚಲಾಯಿಸಿ (Run Server)
```bash
# Production mode
npm start

# Development mode (auto-reload)
npm run dev
```

---

## ☁️ ಡಿಪ್ಲಾಯ್‌ಮೆಂಟ್ (Deployment Guide)

### Render.com ಅಥವಾ Railway.app ನಲ್ಲಿ ಡಿಪ್ಲಾಯ್ ಮಾಡಲು:
1. ನಿಮ್ಮ GitHub ರೆಪೊಸಿಟರಿಯನ್ನು Render / Railway ಗೆ ಸಂಪರ್ಕಿಸಿ.
2. Environment: Node
3. Build Command: `npm install`
4. Start Command: `node server.js`
5. Render ಡ್ಯಾಶ್‌ಬೋರ್ಡ್‌ನಲ್ಲಿ **Environment Variables** ಸೆಕ್ಷನ್‌ಗೆ ಹೋಗಿ ನಿಮ್ಮ `.env` ಕೀಗಳನ್ನು ಸೇರಿಸಿ.
6. Render/Railway ನಲ್ಲಿ FFmpeg ಸಪೋರ್ಟ್ ಇರುವುದರಿಂದ 24/7 ನಿರಂತರವಾಗಿ 10 ನಿಮಿಷದ ಶೆಡ್ಯೂಲರ್ ರನ್ ಆಗುತ್ತದೆ!
