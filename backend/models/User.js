import mongoose from 'mongoose';

const userSchema = new mongoose.Schema({
  email: { type: String, required: true, unique: true },
  name: { type: String, default: "Pro Creator" },
  youtubeTokens: { type: Object }, // YouTube Access & Refresh Tokens
  metaTokens: { type: Object },    // Instagram/Facebook Access Tokens
  promptConfig: {
    topic: { type: String, default: "Fascinating facts that will blow your mind" },
    platform: { type: String, default: "youtube" }, // "youtube", "instagram", "facebook", "all"
    format: { type: String, default: "9:16 Shorts" },
    language: { type: String, default: "en" }, // "en" or "kn"
    autoUploadEnabled: { type: Boolean, default: true }
  },
  createdAt: { type: Date, default: Date.now }
});

export default mongoose.model('User', userSchema);
