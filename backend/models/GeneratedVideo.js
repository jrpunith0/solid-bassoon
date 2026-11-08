import mongoose from 'mongoose';

const generatedVideoSchema = new mongoose.Schema(
  {
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      required: false
    },
    userEmail: {
      type: String,
      default: 'punithjr2@gmail.com',
      index: true
    },
    title: {
      type: String,
      required: [true, 'Video title is required'],
      trim: true
    },
    topic: {
      type: String,
      trim: true
    },
    platform: {
      type: String,
      enum: ['youtube', 'instagram', 'facebook', 'all'],
      default: 'youtube',
      index: true
    },
    format: {
      type: String,
      enum: ['9:16 Shorts', '16:9 Long-Form', '1:1 Square'],
      default: '9:16 Shorts'
    },
    language: {
      type: String,
      default: 'en'
    },
    script: {
      type: String,
      required: true
    },
    description: {
      type: String,
      default: ''
    },
    tags: {
      type: [String],
      default: []
    },
    hashtags: {
      type: String,
      default: ''
    },
    visualPrompts: {
      type: [String],
      default: []
    },
    videoUrl: {
      type: String,
      default: ''
    },
    thumbnailUrl: {
      type: String,
      default: ''
    },
    durationSec: {
      type: Number,
      default: 60
    },
    uploadStatus: {
      type: String,
      enum: ['generating', 'rendered', 'uploading', 'uploaded', 'failed'],
      default: 'generated',
      index: true
    },
    uploadId: {
      type: String,
      default: ''
    },
    publishedUrl: {
      type: String,
      default: ''
    },
    errorMessage: {
      type: String,
      default: ''
    },
    metadata: {
      resolution: { type: String, default: '1080x1920' },
      fps: { type: Number, default: 30 },
      fileSizeMb: { type: Number, default: 0 },
      generatorModel: { type: String, default: 'gemini-1.5-flash' }
    },
    metrics: {
      views: { type: Number, default: 0 },
      likes: { type: Number, default: 0 },
      shares: { type: Number, default: 0 }
    }
  },
  {
    timestamps: true
  }
);

// Indexes for fast dashboard query performance
generatedVideoSchema.index({ createdAt: -1 });
generatedVideoSchema.index({ userEmail: 1, createdAt: -1 });
generatedVideoSchema.index({ platform: 1, uploadStatus: 1 });

export default mongoose.model('GeneratedVideo', generatedVideoSchema);
