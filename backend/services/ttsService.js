import googleTTS from 'google-tts-api';
import fs from 'fs';
import path from 'path';
import fetch from 'node-fetch';

export async function textToSpeech(text, outputPath, lang = 'en') {
  try {
    const cleanText = (text || "Welcome to Creator Studio automated video").slice(0, 200);

    // ಟೆಕ್ಸ್ಟ್ ಅನ್ನು ಆಡಿಯೋ URL ಆಗಿ ಬದಲಾಯಿಸುವುದು
    const url = googleTTS.getAudioUrl(cleanText, {
      lang: lang === 'kn' ? 'kn' : 'en',
      slow: false,
      host: 'https://translate.google.com',
    });

    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`TTS server responded with ${response.status}`);
    }

    const buffer = await response.buffer();
    fs.writeFileSync(outputPath, buffer);
    console.log('✅ ಆಡಿಯೋ ವಾಯ್ಸ್ಓವರ್ ಸಿದ್ಧವಾಗಿದೆ:', outputPath);
    return outputPath;
  } catch (error) {
    console.error('❌ TTS ದೋಷ:', error.message);
    throw error;
  }
}
