import express from 'express';
import GeneratedVideo from '../models/GeneratedVideo.js';

const router = express.Router();

/**
 * GET /api/videos
 * Returns a list of previously generated videos with optional filters for dashboard display.
 */
router.get('/videos', async (req, res) => {
  try {
    const {
      userEmail,
      platform,
      status,
      search,
      limit = 50,
      page = 1,
      sortBy = 'createdAt',
      order = 'desc'
    } = req.query;

    const query = {};

    if (userEmail) {
      query.userEmail = userEmail;
    }

    if (platform && platform !== 'all') {
      query.platform = platform.toLowerCase();
    }

    if (status) {
      query.uploadStatus = status.toLowerCase();
    }

    if (search) {
      query.$or = [
        { title: { $regex: search, $options: 'i' } },
        { topic: { $regex: search, $options: 'i' } },
        { description: { $regex: search, $options: 'i' } }
      ];
    }

    const parsedLimit = Math.min(parseInt(limit, 10) || 50, 100);
    const parsedPage = Math.max(parseInt(page, 10) || 1, 1);
    const skip = (parsedPage - 1) * parsedLimit;
    const sortOrder = order === 'asc' ? 1 : -1;

    const [videos, total] = await Promise.all([
      GeneratedVideo.find(query)
        .sort({ [sortBy]: sortOrder })
        .skip(skip)
        .limit(parsedLimit)
        .lean(),
      GeneratedVideo.countDocuments(query)
    ]);

    res.json({
      success: true,
      data: videos,
      pagination: {
        total,
        page: parsedPage,
        limit: parsedLimit,
        pages: Math.ceil(total / parsedLimit) || 1
      }
    });
  } catch (error) {
    console.error('Error fetching generated videos:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to retrieve generated videos',
      error: error.message
    });
  }
});

/**
 * GET /api/videos/stats/summary
 * Aggregate metrics of generated content for the creator dashboard.
 */
router.get('/videos/stats/summary', async (req, res) => {
  try {
    const { userEmail } = req.query;
    const match = userEmail ? { userEmail } : {};

    const [totalVideos, uploadedVideos, platformStats] = await Promise.all([
      GeneratedVideo.countDocuments(match),
      GeneratedVideo.countDocuments({ ...match, uploadStatus: 'uploaded' }),
      GeneratedVideo.aggregate([
        { $match: match },
        { $group: { _id: '$platform', count: { $sum: 1 } } }
      ])
    ]);

    res.json({
      success: true,
      summary: {
        totalGenerated: totalVideos,
        totalUploaded: uploadedVideos,
        platforms: platformStats.reduce((acc, curr) => {
          acc[curr._id] = curr.count;
          return acc;
        }, {})
      }
    });
  } catch (error) {
    console.error('Error calculating video stats:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to compute video metrics',
      error: error.message
    });
  }
});

/**
 * GET /api/videos/:id
 * Retrieve full details of a specific generated video.
 */
router.get('/videos/:id', async (req, res) => {
  try {
    const video = await GeneratedVideo.findById(req.params.id);
    if (!video) {
      return res.status(404).json({
        success: false,
        message: 'Generated video not found'
      });
    }
    res.json({
      success: true,
      data: video
    });
  } catch (error) {
    console.error('Error fetching video by ID:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to retrieve video',
      error: error.message
    });
  }
});

/**
 * POST /api/videos
 * Track and persist a newly generated or rendered video.
 */
router.post('/videos', async (req, res) => {
  try {
    const {
      userId,
      userEmail,
      title,
      topic,
      platform,
      format,
      language,
      script,
      description,
      tags,
      hashtags,
      visualPrompts,
      videoUrl,
      thumbnailUrl,
      durationSec,
      uploadStatus,
      uploadId,
      publishedUrl,
      metadata
    } = req.body;

    if (!title || !script) {
      return res.status(400).json({
        success: false,
        message: 'Title and script are required fields'
      });
    }

    const newVideo = new GeneratedVideo({
      userId,
      userEmail: userEmail || 'punithjr2@gmail.com',
      title,
      topic: topic || title,
      platform: platform ? platform.toLowerCase() : 'youtube',
      format: format || '9:16 Shorts',
      language: language || 'en',
      script,
      description: description || '',
      tags: Array.isArray(tags) ? tags : [],
      hashtags: hashtags || '',
      visualPrompts: Array.isArray(visualPrompts) ? visualPrompts : [],
      videoUrl: videoUrl || '',
      thumbnailUrl: thumbnailUrl || '',
      durationSec: durationSec || 60,
      uploadStatus: uploadStatus || 'rendered',
      uploadId: uploadId || '',
      publishedUrl: publishedUrl || '',
      metadata: metadata || {}
    });

    const saved = await newVideo.save();

    res.status(201).json({
      success: true,
      message: 'Video generation record tracked successfully',
      data: saved
    });
  } catch (error) {
    console.error('Error tracking generated video:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to save generated video record',
      error: error.message
    });
  }
});

/**
 * PATCH /api/videos/:id
 * Update status, upload details, or performance metrics.
 */
router.patch('/videos/:id', async (req, res) => {
  try {
    const updated = await GeneratedVideo.findByIdAndUpdate(
      req.params.id,
      { $set: req.body },
      { new: true, runValidators: true }
    );

    if (!updated) {
      return res.status(404).json({
        success: false,
        message: 'Video record not found'
      });
    }

    res.json({
      success: true,
      message: 'Video updated successfully',
      data: updated
    });
  } catch (error) {
    console.error('Error updating video record:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to update video record',
      error: error.message
    });
  }
});

/**
 * DELETE /api/videos/:id
 * Delete a past generated video entry from history.
 */
router.delete('/videos/:id', async (req, res) => {
  try {
    const deleted = await GeneratedVideo.findByIdAndDelete(req.params.id);
    if (!deleted) {
      return res.status(404).json({
        success: false,
        message: 'Video record not found'
      });
    }

    res.json({
      success: true,
      message: 'Video record deleted from history'
    });
  } catch (error) {
    console.error('Error deleting video record:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to delete video record',
      error: error.message
    });
  }
});

export default router;
