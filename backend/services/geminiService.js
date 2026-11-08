import { GoogleGenAI } from "@google/genai";

// Initialize Gemini API with your Google AI Studio key
function getAIClient() {
  const apiKey = process.env.GEMINI_API_KEY || "";
  if (!apiKey) return null;
  try {
    return new GoogleGenAI({ apiKey });
  } catch (e) {
    return null;
  }
}

export async function generateVideoContent(topic, platform = "youtube", language = "en") {
  try {
    const ai = getAIClient();
    if (!ai) throw new Error("GEMINI_API_KEY not configured, using fallback generator");
    const isKn = language === "kn";
    const prompt = `
      Create a viral ${platform} short-form video script about: "${topic}".
      Target Language: ${isKn ? "Kannada (ಕನ್ನಡ)" : "English"}.
      Return ONLY a valid JSON object with the following keys:
      - "title": Catchy high CTR title with hashtags
      - "description": SEO optimized description with keywords and call-to-action
      - "script": The spoken voiceover text (under 60 words, engaging hook first)
      - "visualPrompts": An array of exactly 3 distinct cinematic image generation prompts describing Scene 1, Scene 2, and Scene 3 in photorealistic detail.
    `;

    const response = await ai.models.generateContent({
      model: "gemini-2.5-flash",
      contents: prompt,
      config: {
        responseMimeType: "application/json"
      }
    });

    return JSON.parse(response.text);
  } catch (error) {
    console.error("Gemini Generation Error:", error.message);
    // Graceful fallback for offline/unconfigured environments
    return {
      title: `${topic} - Viral Breakdown #Shorts`,
      description: `In this video we explore ${topic}. Like, comment and subscribe for more creator content! #CreatorStudio #AI #Shorts`,
      script: `Stop scrolling! Did you know this secret about ${topic}? Here are three reasons why this changes everything. Try this today and save this video!`,
      visualPrompts: [
        `Cinematic neon studio close-up illustrating ${topic}, 8k photorealistic volumetric lighting`,
        `Dynamic glowing tech HUD diagram explaining ${topic} concept with vibrant particles`,
        `High-energy creator in modern aesthetic room gesturing towards subscribe icon`
      ]
    };
  }
}
