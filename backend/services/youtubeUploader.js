import { google } from 'googleapis';
import fs from 'fs';

export async function uploadToYouTube(authTokens, videoPath, metadata) {
  const oauth2Client = new google.auth.OAuth2(
    process.env.GOOGLE_CLIENT_ID,
    process.env.GOOGLE_CLIENT_SECRET,
    process.env.GOOGLE_REDIRECT_URI || "http://localhost:5000/api/auth/youtube/callback"
  );
  oauth2Client.setCredentials(authTokens);

  const youtube = google.youtube({ version: 'v3', auth: oauth2Client });

  try {
    const res = await youtube.videos.insert({
      part: ['snippet', 'status'],
      requestBody: {
        snippet: {
          title: metadata.title || "AI Generated Video #Shorts",
          description: metadata.description || "Created with Creator Studio",
          tags: ['shorts', 'viral', 'ai', 'creatorstudio'],
          categoryId: '28', // Science & Technology
        },
        status: {
          privacyStatus: 'public', // 'public', 'unlisted', or 'private'
          selfDeclaredMadeForKids: false,
        },
      },
      media: {
        body: fs.createReadStream(videoPath),
      },
    });

    console.log('✅ ಯೂಟ್ಯೂಬ್ಗೆ ವೀಡಿಯೊ ಅಪ್ಲೋಡ್ ಯಶಸ್ವಿಯಾಗಿದೆ! Video ID:', res.data.id);
    return res.data.id;
  } catch (error) {
    console.error('❌ ಯೂಟ್ಯೂಬ್ ಅಪ್ಲೋಡ್ ದೋಷ:', error.message);
    throw error;
  }
}
