import express from 'express';
import mongoose from 'mongoose';
import dotenv from 'dotenv';
import cors from 'cors';
import cron from 'node-cron';
import path from 'path';
import fs from 'fs';

import User from './models/User.js';
import GeneratedVideo from './models/GeneratedVideo.js';
import { generateVideoContent } from './services/geminiService.js';
import { textToSpeech } from './services/ttsService.js';
import { fetchImagesForPrompt } from './services/imageService.js';
import { renderVideo } from './services/videoRenderer.js';
import { uploadToYouTube } from './services/youtubeUploader.js';
import authRoutes from './routes/authRoutes.js';
import videoRoutes from './routes/videoRoutes.js';

import syncRoutes from './routes/syncRoutes.js';

dotenv.config();
const app = express();

app.use(express.json());
app.use(cors());

// Serve static web application for Windows, Mac, Linux and mobile browsers
const possibleWebPaths = [
  path.join(process.cwd(), '../web'),
  path.join(process.cwd(), 'web'),
  path.join(process.cwd(), '../public'),
  path.join(process.cwd(), 'public')
];
const webPath = possibleWebPaths.find(p => fs.existsSync(p)) || path.join(process.cwd(), '../web');
app.use(express.static(webPath));
app.use('/web', express.static(webPath));

app.use('/api', authRoutes);
app.use('/api', videoRoutes);
app.use('/api/sync', syncRoutes);

// Root Health Check Route
app.get('/api/health', (req, res) => {
  res.json({
    app: "Creator Studio Pro Universal Cross-Platform Engine",
    status: "running",
    platforms: ["Web Desktop/Browser", "Android App", "iOS App"],
    supportedEngines: ["Gemini Pro", "OpenAI", "Runway Gen-3", "Kling AI", "ElevenLabs"]
  });
});

// ----------------------------------------------------
// ⏱️ 10 ನಿಮಿಷಕ್ಕೊಮ್ಮೆ ರನ್ ಆಗುವ ಆಟೋಮೇಟೆಡ್ Cron Job (Auto-Error Fixed & Crash-Proof)
// ----------------------------------------------------
cron.schedule('*/10 * * * *', async () => {
  console.log('⏱ [Cron Job] 10 ನಿಮಿಷದ ಆಟೋ ವೀಡಿಯೊ ಪ್ರಕ್ರಿಯೆ ಆರಂಭವಾಗಿದೆ...');

  try {
    const activeUsers = await User.find({ 'promptConfig.autoUploadEnabled': true });

    if (!activeUsers || activeUsers.length === 0) {
      console.log('ℹ️️ ಆಟೋ-ಅಪ್ಲೋಡ್ ಆನ್ ಮಾಡಿರುವ ಯಾವುದೇ ಬಳಕೆದಾರರು ಸಿಗಲಿಲ್ಲ (Waiting for configured users).');
      return;
    }

    for (const user of activeUsers) {
      // ಪ್ರತಿಯೊಬ್ಬ ಬಳಕೆದಾರರನ್ನು ಪ್ರತ್ಯೇಕವಾಗಿ ಟ್ರೈ-ಕ್ಯಾಚ್ನಲ್ಲಿ ಇರಿಸಲಾಗಿದೆ, 
      // ಇದರಿಂದ ಒಬ್ಬರಿಗೆ ಎರರ್ ಬಂದ್ರು ಬೇರೆಯವರ ವೀಡಿಯೊ ಕಟ್ ಆಗುವುದಿಲ್ಲ.
      try {
        console.log(`👤 ಬಳಕೆದಾರರಿಗಾಗಿ ವೀಡಿಯೊ ಸಿದ್ಧಪಡಿಸಲಾಗುತ್ತಿದೆ: ${user.email}`);
        
        const topic = user.promptConfig?.topic || "Fascinating facts that blow your mind";
        const platform = user.promptConfig?.platform || "youtube";
        const language = user.promptConfig?.language || "en";

        // 1. Google AI Studio Gemini ಕಂಟೆಂಟ್
        const content = await generateVideoContent(topic, platform, language);
        console.log('📝 Gemini ಕಂಟೆಂಟ್ ಸಿದ್ಧವಾಗಿದೆ:', content.title);

        // ಡೈರೆಕ್ಟರಿ ಖಚಿತಪಡಿಸಿಕೊಳ್ಳುವುದು
        const downloadDir = path.join(process.cwd(), 'downloads');
        if (!fs.existsSync(downloadDir)) {
          fs.mkdirSync(downloadDir, { recursive: true });
        }

        // 2. Text-to-Speech (ಧ್ವನಿ ರೆಕಾರ್ಡಿಂಗ್)
        const audioPath = path.join(downloadDir, `audio_${Date.now()}.mp3`);
        await textToSpeech(content.script, audioPath, language);

        // 3. 3 Visual Scenes (ದೃಶ್ಯಗಳು)
        const visualQuery = content.visualPrompts?.[0] || topic;
        const imagePaths = await fetchImagesForPrompt(visualQuery, 3);

        // 4. FFmpeg Video Render
        const videoOutputPath = path.join(downloadDir, `video_${Date.now()}.mp4`);
        await renderVideo(imagePaths, audioPath, videoOutputPath);

        // 5. YouTube Shorts / Social Media Upload
        let uploadId = '';
        let publishedUrl = '';
        let uploadStatus = 'rendered';

        if (platform === 'youtube' && user.youtubeTokens) {
          try {
            const uploadRes = await uploadToYouTube(user.youtubeTokens, videoOutputPath, content);
            uploadId = uploadRes?.id || `yt_${Date.now()}`;
            publishedUrl = `https://studio.youtube.com/video/${uploadId}/edit`;
            uploadStatus = 'uploaded';
            console.log(`✅ [Success] ${user.email} ಗಾಗಿ ವೀಡಿಯೊ ಯಶಸ್ವಿಯಾಗಿ ಅಪ್ಲೋಡ್ ಆಗಿದೆ!`);
          } catch (uploadErr) {
            uploadStatus = 'failed';
            console.error('⚠️ YouTube ಅಪ್ಲೋಡ್ ದೋಷ:', uploadErr.message);
          }
        } else {
          console.log(`ℹ️ [Rendered] ವೀಡಿಯೊ ಸಿದ್ಧವಾಗಿದೆ: ${videoOutputPath}. ಟೋಕನ್ ಪರಿಶೀಲಿಸಿ.`);
        }

        // 5.1 MongoDB ನಲ್ಲಿ ವೀಡಿಯೊ ವಿವರಗಳನ್ನು ಉಳಿಸುವುದು (Track in MongoDB)
        try {
          await GeneratedVideo.create({
            userId: user._id,
            userEmail: user.email || 'punithjr2@gmail.com',
            title: content.title || topic,
            topic: topic,
            platform: platform || 'youtube',
            format: user.promptConfig?.format || '9:16 Shorts',
            language: language || 'en',
            script: content.script || '',
            description: content.description || '',
            visualPrompts: content.visualPrompts || [],
            durationSec: 60,
            uploadStatus: uploadStatus,
            uploadId: uploadId,
            publishedUrl: publishedUrl
          });
          console.log(`💾 [MongoDB] ವೀಡಿಯೊ ವಿವರಗಳು ಯಶಸ್ವಿಯಾಗಿ ಟ್ರ್ಯಾಕ್ ಆಗಿವೆ: ${content.title}`);
        } catch (dbSaveErr) {
          console.warn('⚠️ MongoDB ಸೇವ್ ದೋಷ:', dbSaveErr.message);
        }

        // 6. ಕ್ಲೀನಪ್ (ಬಳಕೆಯಾದ ಟೆಂಪರರಿ ಫೈಲ್ಗಳನ್ನು ಡಿಲೀಟ್ ಮಾಡುವುದು)
        setTimeout(() => {
          try {
            if (fs.existsSync(audioPath)) fs.unlinkSync(audioPath);
            if (fs.existsSync(videoOutputPath)) fs.unlinkSync(videoOutputPath);
            imagePaths.forEach(img => {
              if (fs.existsSync(img)) fs.unlinkSync(img);
            });
            console.log('🧹 ಟೆಂಪರರಿ ಫೈಲ್ಗಳು ಕ್ಲೀನ್ ಆಗಿವೆ.');
          } catch (cleanErr) {
            console.error('⚠️️ ಫೈಲ್ ಡಿಲೀಟ್ ಮಾಡುವಲ್ಲಿ ಸಣ್ಣ ದೋಷ:', cleanErr.message);
          }
        }, 5000);

      } catch (userError) {
        console.error(`❌ ಬಳಕೆದಾರರಾದ ${user.email} ರವರ ಪ್ರಕ್ರಿಯೆಯಲ್ಲಿ ದೋಷ:`, userError.message);
        // ಲೂಪ್ ಮುಂದುವರಿಯುತ್ತದೆ, ಮುಂದಿನ ಯೂಸರ್ಗೆ ತೊಂದರೆಯಾಗುವುದಿಲ್ಲ
      }
    }
  } catch (error) {
    console.error('❌ [Cron Job Global Error]:', error.message);
  }
});

// ----------------------------------------------------
// ಡೇಟಾಬೇಸ್ ಕನೆಕ್ಷನ್ ಮತ್ತು ಸರ್ವರ್ ಸ್ಟಾರ್ಟ್
// ----------------------------------------------------
const PORT = 3000;
const mongoUri = process.env.MONGO_URI || "mongodb://127.0.0.1:27017/creator_studio";

// Start HTTP Server immediately on PORT 3000 to serve web and APIs
app.listen(PORT, () => {
  console.log(`🚀 Creator Studio Pro Web & API Server is live at: http://localhost:${PORT}`);
});

// Asynchronous MongoDB connection attempt
mongoose.connect(mongoUri, { serverSelectionTimeoutMS: 2000 })
  .then(() => {
    console.log('🗄️ MongoDB ಡೇಟಾಬೇಸ್ ಯಶಸ್ವಿಯಾಗಿ ಕನೆಕ್ಟ್ ಆಗಿದೆ.');
  })
  .catch((err) => {
    console.warn('⚠️ MongoDB ಕನೆಕ್ಷನ್ ಲಭ್ಯವಿಲ್ಲ (Offline/In-memory fallback mode active):', err.message);
  });
